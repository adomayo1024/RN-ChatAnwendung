package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.BCPPacket;
import ChatAnwendung.logic.Api.ReceiveHandler;
import ChatAnwendung.persistence.Api.*;
import ChatAnwendung.Exceptions.IllegalSequenzNumberException;
import ChatAnwendung.Exceptions.ExceptionHandler;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Impl.FileImpl;
import ChatAnwendung.persistence.Impl.RoutingEntryImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class ReceiveHandlerImpl implements ReceiveHandler {


    //Die Receive-queue, wo all Pakete vom Receiver eingepackt werden
    private final BlockingQueue<DatagramPacket> receiverQueue;

    //Die send-queue, damit der Sender die Pakete senden kann.
    private final BlockingQueue<DatagramPacket> senderQueue;

    //Die Routing table
    private final RoutingTable routingTable;

    //Storage für die eigene Informationen
    private final Storage storage;

    //Downloader für die heruntergeladenen Dateien
    private final DownloadFiles downloadFiles;

    //Ob der Receiver beendet werden soll
    private boolean interrupted;

    public ReceiveHandlerImpl(BlockingQueue<DatagramPacket> receiverQueue, BlockingQueue<DatagramPacket> senderQueue, RoutingTable routingTable, Storage storage, DownloadFiles downloadFiles){
        this.receiverQueue = receiverQueue;
        this.senderQueue = senderQueue;
        this.routingTable = routingTable;
        this.storage = storage;
        this.downloadFiles = downloadFiles;
        interrupted = false;
    }

    @Override
    public void run(){
        DatagramPacket packet;

        //Loop, wo alle Pakete verarbeitet werden.
        while(!interrupted){

            //Pakete werden geholt
            try {
                packet = receiverQueue.take();
            } catch (InterruptedException e) {
               interrupted = true;
               continue;
            }

            //Wird in ein BCPPacket umgewandelt.
            BCPPacket bcpPacket = new BCPPacketImpl(packet);

            //Wenn nicht eingeloggt, sollen die Pakete ignoriert werden.
            if(!storage.isLogin()){
                log.debug("Packet throw away from: {} because not logged in", packet.getSocketAddress());
            }
            //Checksumme wird auf korrektheit überprüft
            else if(bcpPacket.getCrc() != bcpPacket.calculateCrc()){
                log.debug("Packet CRC mismatch");
            }
            //Überprüft, ob Packet für einen selber ist.
            else if (bcpPacket.isItForMe(storage.getID())){
                //Spezielle Verarbeitung je nach Packet Typ,
                switch (bcpPacket.getType()){
                    case PacketTypes.HELLO -> handleHello(bcpPacket);

                    case PacketTypes.WELCOME -> handleWelcome(bcpPacket);

                    case PacketTypes.BYE -> handleGoodbye(bcpPacket);

                    case PacketTypes.FILE_INIT -> handleFileInit(bcpPacket);

                    case PacketTypes.FILE_DATA -> handleFileData(bcpPacket);

                    case PacketTypes.File_End -> handleFileEnd(bcpPacket);

                    case PacketTypes.RESEND_REQUEST -> handleResendRequest(bcpPacket);

                    case PacketTypes.MESSAGE -> handleMessage(bcpPacket);

                    case PacketTypes.HEARTBEAT -> handleHeartbeat(bcpPacket);

                    case PacketTypes.ROUTING_TABLE -> handleRoutingTable(bcpPacket);
                }
            }
            //Paket wird weitergeleitet
            else {
                handleFeedForwarding(bcpPacket);
            }
        }
    }

    /**
     * Verarbeitet des RoutingTable Paketes.
     * @param packet Packet, welches in Payload die RoutingTableEinträge des Senders enthält.
     */
    private void handleRoutingTable(BCPPacket packet) {
        log.debug("Received Routing Table");

        InetAddress srcAddress = packet.getAddress();
        int srcPort = packet.getPort();
        int payloadLength = packet.getPayloadLength();

        //Es werden alle RoutingTableEinträge im Payload verarbeitet.
        for(int offset = 0; offset < payloadLength; offset += BCPPacket.ROUTING_TABLE_ENTRY_SIZE){
            long nodeId = packet.getNodeIdFromRoutingTableEntry(offset);
            byte hops = packet.getHopsFromRoutingTableEntry(offset);
            long lastSeen = packet.getLastSeenFromRoutingTableEntry(offset);

            RoutingEntry entry = new RoutingEntryImpl(nodeId, srcAddress, srcPort, ++hops, lastSeen);

            //Hinzufüge des Eintrags zur RoutingTable
            routingTable.add(entry);

            log.debug("Routing Entry added for {}", Long.toUnsignedString(nodeId));
            log.info("User: {} is available for Chatting", Long.toUnsignedString(nodeId));
        }
    }

    /**
     * Verarbeitet das Heartbeat Paket. Welches angibt, dass der Sender noch verfügbar ist.
     * @param packet Das Heartbeat Paket.
     */
    private void handleHeartbeat(BCPPacket packet) {
        log.debug("Received Heartbeat");

        long srcNodeId = packet.getSrcNodeId();

        //Aktualisiert den LastSeen Wert des Senders.
        routingTable.setLastSeen(srcNodeId);

        log.debug("Last seen set for {}", Long.toUnsignedString(srcNodeId));
    }

    /**
     * Verarbeitet das Nachrichtenpaket. Und gibt die Nachricht aus.
     * @param packet Das Nachrichtenpaket, die Nachricht steht im Payload.
     */
    private void handleMessage(BCPPacket packet) {
        log.debug("Received Message");

        //Ausgabe der Nachricht in der Konsole.
        String terminalOutput = "You received a message from: " +
                Long.toUnsignedString(packet.getSrcNodeId()) +
                ": " +
                new String(packet.getPayload(), StandardCharsets.UTF_8);

        System.out.println(terminalOutput);

        log.debug("Message received end");
    }

    /**
     * Verarbeitet ein ResendRequest Paket. Worauf hin ein File-Data Packet mit dem konkreten Chunk gesendet werden soll.
     * @param packet Das ResendRequest Packet, wo im Header die fileId und die Sequenz für den Chunk steht.
     */
    private void handleResendRequest(BCPPacket packet) {
        log.debug("Received Request");


        int sequenz = packet.getSequenz();
        int fileId = packet.getFileId();
        long srcNodeId = packet.getSrcNodeId();
        InetAddress srcAddress = packet.getAddress();
        int srcPort = packet.getPort();
        String filePath = storage.getOpenFile(fileId);
        byte[] payload;


        //Holt den Chunk aus dem Dateipfad.
        try(RandomAccessFile file = new RandomAccessFile(filePath, "r")){
            int anzahlChunks = (int) Math.ceil((double) file.length() / BCPPacket.MAXIMUM_PAYLOAD_SIZE);
            payload = getChunk(file, sequenz, anzahlChunks);
        } catch (IOException e){
            return;
        }


        //Setzt die BCP-Paket-Parameter von dem erhaltenen Paket, um kein neues Packet zu erstellen.
        packet.setType(PacketTypes.FILE_DATA);
        packet.setHops((byte)0);
        packet.setTtl((byte) 32);
        packet.setSrcNodeId(storage.getID());
        packet.setDestNodeId(srcNodeId);
        packet.setFileId(fileId);
        packet.setSequenz(sequenz);
        packet.setPayloadLength((short)payload.length);
        packet.setPayload(payload);
        packet.setAddress(srcAddress);
        packet.setPort(srcPort);


        DatagramPacket requestPacket = packet.makeDatagramPacket();

        senderQueue.add(requestPacket);

        log.debug("Send Request for FileID {} and sequence: {} from the User: {}", fileId, sequenz, Long.toUnsignedString(srcNodeId));

    }

    /**
     * Gibt den Chunk, der File wieder, von der angegebenen Position. Wenn es der letzte Chunk der file ist,
     * kann es sein, dass das Byte-Array kleiner ist als {@code BCPPacket.getMaximumPayloadSize()}
     * @param file Die File, von der der Chunk geholt werden soll.
     * @param sequenz Die Position des Chunks in der Datei.
     * @param anzahlChunks Die Anzahl der Chunks in der Datei.
     * @return Byte-Array des Chunks, oder null, wenn die Sequenz fehlerhaft ist, oder es zu einem IO/Error kommt.
     */
    private byte[] getChunk(RandomAccessFile file, int sequenz, int anzahlChunks) {
        byte[] chunk = null;

        try {
            //Prüft, ob die Sequenz korrekt ist.
            if(anzahlChunks <= sequenz || sequenz < 0){
                throw new IllegalSequenzNumberException(sequenz);
            }
            //Prüft, ob es der letzte Chunk ist.
            else if(anzahlChunks - 1 == sequenz){
                int size = (int)(file.length() % BCPPacket.MAXIMUM_PAYLOAD_SIZE);
                chunk = new byte[size];
            }
            else{
                chunk = new byte[BCPPacket.MAXIMUM_PAYLOAD_SIZE];
            }
            //List den Chunk aus der Datei.
            file.seek((long) sequenz * BCPPacket.MAXIMUM_PAYLOAD_SIZE);
            file.read(chunk);
        } catch (IOException | IllegalSequenzNumberException e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        return chunk;
    }

    /**
     * Verarbeitet File-End Packet. Welches suggeriert, dass alle Chunks der Datei versendet wurden.
     * Tut bei mir nichts, weil es verloren gehen kann, wodurch das Requesting nicht gestartet werden kann.
     * @param packet Das File-End Packet, mit FileId im Header.
     */
    private void handleFileEnd(BCPPacket packet) {

        log.debug("Received File End from User: {} and File: {}", packet.getSrcNodeId(), packet.getFileId());

//        File file = downloadFiles.getFile(packet.getSrcNodeId(), packet.getFileId());
//
//        file.startRequesting(downloadFiles, routingTable, storage, senderQueue);

    }

    /**
     * Verarbeitet File-Init Packet. Welches ankündigt, dass eine Datei gesendet werden soll.
     * Es wird ein File erzeugt.
     * @param packet File-Init Packet, mit Anzahl an chunks und fileId im Header. Und Größe und Name im Payload.
     */
    private void handleFileInit(BCPPacket packet) {
        log.debug("Received File Init");

        int anzahlChunks = packet.getSequenz();
        int fileID = packet.getFileId();
        long srcUID = packet.getSrcNodeId();
        String fileName = packet.getFileName();
        int size = packet.getFileSize();

        //Erstellt ein neues File Objekt.
        FileImpl file = new FileImpl(
                anzahlChunks,
                size,
                fileName,
                fileID,
                srcUID);

        //Fügt file zu den zu downloaden Dateien hinzu.
        downloadFiles.setNewFile(srcUID, fileID, file);

        // Es wird das Requesting gestartet, aber erst nach 3 Sekunden (3 Sekunden ist ein zufallällig gewählter Wert)
        downloadFiles.startRequesting(file, downloadFiles, routingTable, storage, senderQueue);

        log.debug("Created new File{} for: {} from User: {}", fileName, fileID, Long.toUnsignedString(srcUID));

        log.info("Starting with downloading of File: {} from User: {}", fileName, Long.toUnsignedString(srcUID));
        System.out.println("Starting with downloading of File: " + fileName + " from User: " + Long.toUnsignedString(srcUID));

    }

    /**
     * Verarbeitet File-Data packet. Fügt den im Packet enthaltenen Chunk dem entsprechenden File hinzu.
     * @param packet Das File-Data Packet, welches im Header die Sequence hat und die FileId. Und im Payload die Bytes des Chunks.
     */
    private void handleFileData(BCPPacket packet) {
        log.debug("Received File Data");

        long srcUID = packet.getSrcNodeId();
        int fileId = packet.getFileId();
        int sequenz = packet.getSequenz();
        byte[] payload = packet.getPayload();


        //holt File 
        File file = downloadFiles.getFile(srcUID, fileId);

        //Prüft, ob die File vorhanden ist.
        if(file != null){
            //Fügt den Chunk dem File hinzu.
            if (file.addChunk(payload, sequenz)) {
                log.debug("Added Chunk: {} to File: {}from User: {}", sequenz, file.getName(), Long.toUnsignedString(srcUID));
            }
            //Prüft, ob die Datei komplett ist, wenn ja, wird sie gespeichert.
            if(file.finished()){
                file.safeFile();
                downloadFiles.stopRequesting(file);
                downloadFiles.removeFile(srcUID, fileId);
                log.info("Finished downloading File: {} from User: {}", file.getName(), Long.toUnsignedString(srcUID));
                System.out.println("Finished downloading File: " + file.getName() + " from User: " + Long.toUnsignedString(srcUID));
            }
        }
        else{
            log.debug("Failed to receive the file: {} from the user: {} ", fileId, srcUID);
        }
    }

    /**
     * Verarbeitet goodbye Packet. Welches vermittelt, dass der User sich aus dem Chat verlassen hat und nicht mehr verfügbar ist.
     * @param packet Das Goodbye Packet.
     */
    private void handleGoodbye(BCPPacket packet) {
        log.debug("Received Goodbye");

        //Holt die SrcNodeId und entfernt den User mit der Id aus der RoutingTable.
        long srcNodeId = packet.getSrcNodeId();
        routingTable.removeUIDThroughGoodbye(srcNodeId);


        log.debug("UID: {} removed", Long.toUnsignedString(srcNodeId));
        log.info("User: {} left the Chat", Long.toUnsignedString(srcNodeId));

        System.out.println("User: " + Long.toUnsignedString(srcNodeId) + " left the Chat");
    }

    /**
     * Verarbeitet Welcome packet, welches als Antwort zum Hello Packet geschickt wird. Und suggeriert, dass der Sender 
     * verfügbar ist.
     * @param packet Das Welcome Packet.
     */
    private void handleWelcome(BCPPacket packet) {
        log.debug("Received Welcome");

        //Holt relevante Informationen aus dem Packet, für den RoutingTableEintrag.
        long srcNodeId = packet.getSrcNodeId();
        InetAddress srcAddress = packet.getAddress();
        int srcPort = packet.getPort();
        byte hops = packet.getHops();
        long lastSeen = System.currentTimeMillis();
        
        //Erstellt den RoutinTableEintrag.
        RoutingEntry entry = new RoutingEntryImpl(
                srcNodeId,
                srcAddress,
                srcPort,
                ++hops,
                lastSeen
        );

        //Fügt den Eintrag zur RoutingTable hinzu.
        routingTable.add(entry);

        log.debug("Routing Entry added for {}", Long.toUnsignedString(srcNodeId));
        log.info("User: {} is available for Chatting", Long.toUnsignedString(srcNodeId));
        System.out.println("User: " + Long.toUnsignedString(srcNodeId) + " is available for Chatting");
    }

    /**
     * Verarbeitet Hello-Packet. Welches einen neuen User ankündigt, und ein neuer RoutingTableEintrag erstellt wird.
     * Als Antwort wird ein Welcome-Packet gesendet.
     * @param packet Das Hello Packet.
     */
    private void handleHello(BCPPacket packet) {
        log.debug("Received Hello");

        //Holt relevante Informationen aus dem Packet, für den RoutingTableEintrag.
        long srcNodeId = packet.getSrcNodeId();
        InetAddress srcAddress = packet.getAddress();
        int srcPort = packet.getPort();
        byte hops = packet.getHops();
        long lastSeen = System.currentTimeMillis();

        RoutingEntry entry = new RoutingEntryImpl(
                srcNodeId, srcAddress,
                srcPort,
                ++hops,
                lastSeen
        );

        //Fügt den Eintrag zur RoutingTable hinzu.
        routingTable.add(entry);

        //Erstellung des Welcome Packets aus dem erhalten Hello Packet, um kein komplett neues Packet zu erstellen.
        packet.setType(PacketTypes.WELCOME);
        packet.setHops((byte)0);
        packet.setTtl((byte) 32);
        packet.setSrcNodeId(storage.getID());
        packet.setDestNodeId(srcNodeId);
        packet.setFileId(0);
        packet.setSequenz(0);
        packet.setPayloadLength((short)0);
        packet.setPayload(new byte[0]);
        packet.setAddress(srcAddress);
        packet.setPort(srcPort);

        DatagramPacket welcomePacket = packet.makeDatagramPacket();

        senderQueue.add(welcomePacket);

        log.debug("Send Welcome packet to: {}" , Long.toUnsignedString(srcNodeId));

        log.info("User {} joined the Chat", Long.toUnsignedString(srcNodeId));

        System.out.println("User: " + Long.toUnsignedString(srcNodeId) + " joined the Chat");
    }

    /**
     * Leitet ein erhaltenes Packet weiter, da es nicht für diesen User ist.
     * Schickt es an den User weiter, welcher als NextHop in der RoutingTable steht.
     * @param packet Das erhaltene Packet.
     */
    private void handleFeedForwarding(BCPPacket packet){
        log.debug("Start with feed forwarding");

        packet.dekrementTtl();
        long destId = packet.getDestNodeId();
        InetAddress nextHopAddress = routingTable.getNextHopAddressForUID(destId);
        int nextHopPort = routingTable.getNextHopPortForUID(destId);

        //Prüft, ob es weitergeleitet werden soll/kann
        if(packet.getTtl() > 0 || nextHopAddress == null || nextHopPort == -1){
            packet.inkrementHops();
            packet.setAddress(nextHopAddress);
            packet.setPort(nextHopPort);
            DatagramPacket dP = packet.makeDatagramPacket();
            senderQueue.add(dP);

            log.debug("Packet forwarded");
        }
        else {
            log.debug("Packet throw away from: {}", destId);
        }
    }
}
