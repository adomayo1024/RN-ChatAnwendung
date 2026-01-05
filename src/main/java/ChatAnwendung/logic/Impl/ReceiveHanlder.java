package ChatAnwendung.logic.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.Exceptions.IllegalSequnzNumberException;
import ChatAnwendung.Exceptions.ExceptionHandler;
import ChatAnwendung.logic.Enums.PacketTypes;
import ChatAnwendung.persistence.Impl.DownloadFiles;
import ChatAnwendung.persistence.Impl.File;
import ChatAnwendung.persistence.Impl.RoutingEntryImpl;
import ChatAnwendung.persistence.Impl.Storage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class ReceiveHanlder implements Runnable {


    private final BlockingQueue<DatagramPacket> receiverQueue;

    private final BlockingQueue<DatagramPacket> senderQueue;

    private final RoutingTable routingTable;

    private final Storage storage;

    private final DownloadFiles downloadFiles;

    private boolean interrupted;

    public ReceiveHanlder(BlockingQueue<DatagramPacket> receiverQueue, BlockingQueue<DatagramPacket> senderQueue, RoutingTable routingTable, Storage storage, DownloadFiles downloadFiles){
        this.receiverQueue = receiverQueue;
        this.senderQueue = senderQueue;
        this.routingTable = routingTable;
        this.storage = storage;
        this.downloadFiles = downloadFiles;
        interrupted = false;
    }

    public void run(){
        DatagramPacket packet;

        while(!interrupted){

            try {
                packet = receiverQueue.take();
            } catch (InterruptedException e) {
               interrupted = true;
               continue;
            }

            BCPPacket bcpPacket = new BCPPacket(packet);

            if(!storage.isLogin()){
                log.debug("Packet throw away from: {} because not logged in", packet.getSocketAddress());
            } else if(bcpPacket.getCrc() != bcpPacket.calculateCrc()){
                log.debug("Packet CRC mismatch");
            }else if (bcpPacket.isItForMe(storage.getID())){
                switch (bcpPacket.getType()){
                    case PacketTypes.HELLO -> handleHello(bcpPacket);

                    case PacketTypes.WELCOME -> handleWelcome(bcpPacket);

                    case PacketTypes.BYE -> handleGoodbye(bcpPacket);

                    case PacketTypes.FILE_INIT -> handleFileInit(bcpPacket);

                    case PacketTypes.FILE_DATA -> handleFileData(bcpPacket);

                    case PacketTypes.File_End -> handleFileEnd(bcpPacket);

                    case PacketTypes.RESENDREQUEST -> handleResendRequest(bcpPacket);

                    case PacketTypes.MESSAGE -> handleMessage(bcpPacket);

                    case PacketTypes.HEARTBEAT -> handleHeartbeat(bcpPacket);

                    case PacketTypes.ROUTINGTABLE -> handleRoutingTable(bcpPacket);
                }
            }else {
                handleFeedForwading(bcpPacket);
            }
        }


    }

    private void handleRoutingTable(BCPPacket packet) {
        log.debug("Received Routing Table");

        InetAddress srcAdress = packet.getAddress();
        int srcPort = packet.getPort();
        int payloadLength = packet.getPayloadLength();
        int routingEntrySize = routingTable.getRoutingEntrySize();

        for(int offset = 0; offset < payloadLength; offset += routingEntrySize){
            long uID = packet.getNodeIdFromRoutingTableEntry(offset);
            byte hops = packet.getHopsFromRoutingTableEntry(offset);
            long lastSeen = packet.getLastSeenFromRoutinTableEntry(offset);

            RoutingEntry entry = new RoutingEntryImpl(uID, srcAdress, srcPort, (byte) (hops + 1), lastSeen);
            routingTable.add(entry);

            log.debug("Routing Entry added for {}", Long.toUnsignedString(uID));
            log.info("User: {} is available for Chatting", Long.toUnsignedString(uID));

        }
    }

    private void handleHeartbeat(BCPPacket packet) {
        log.debug("Received Heartbeat");


        long srcNodeId = packet.getSrcNodeId();
        routingTable.setLastSeen(srcNodeId);

        log.debug("Last seen set for {}", Long.toUnsignedString(srcNodeId));
    }

    private void handleMessage(BCPPacket packet) {
        log.debug("Received Message");

        String terminalOutput = "You received a message from: " +
                Long.toUnsignedString(packet.getSrcNodeId()) +
                ": " +
                new String(packet.getPayload(), StandardCharsets.UTF_8);

        System.out.println(terminalOutput);

        log.debug("Message received end");
    }

    private void handleResendRequest(BCPPacket packet) {
        log.debug("Received Request");


        int sequenz = packet.getSequenz();
        int fileId = packet.getFileId();
        long srcNodeId = packet.getSrcNodeId();
        InetAddress srcAddress = packet.getAddress();
        int srcPort = packet.getPort();
        String filePath = storage.getOpenFile(fileId);
        byte[] payload;


        try(RandomAccessFile file = new RandomAccessFile(filePath, "r")){
            int anzahlChunks = (int) Math.ceil(file.length() / 1300.0);
            payload = split(file, sequenz, anzahlChunks);
        } catch (IOException e){
            return;
        }


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

    private byte[] split(RandomAccessFile file, int sequenz, int anzahlChunks) {
        byte[] chunk = null;

        try {
            if(anzahlChunks <= sequenz || sequenz < 0){
                throw new IllegalSequnzNumberException(sequenz);
            }
            else if(anzahlChunks - 1 == sequenz){
                int size = (int)(file.length() % 1300);
                chunk = new byte[size];
            }
            else{
                chunk = new byte[1300];
            }
            file.seek(sequenz * 1300L);
            file.read(chunk);
        } catch (IOException | IllegalSequnzNumberException e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        return chunk;
    }

    private void handleFileEnd(BCPPacket packet) {

        log.debug("Received File End from User: {} and File: {}", packet.getSrcNodeId(), packet.getFileId());

        File file = downloadFiles.getFile(packet.getSrcNodeId(), packet.getFileId());

        file.startRequesting(downloadFiles, routingTable, storage, senderQueue);

    }

    private void handleFileInit(BCPPacket packet) {
        log.debug("Received File Init");

        int anzahlChunks = packet.getSequenz();
        int fileID = packet.getFileId();
        long srcUID = packet.getSrcNodeId();
        String fileName = packet.getFileName();
        int size = packet.getFileSize();
        ScheduledExecutorService timer = downloadFiles.getScheduledThreadPool();

        File file = new File(
                anzahlChunks,
                size,
                fileName,
                fileID,
                srcUID,
                timer);

        downloadFiles.setNewFile(srcUID, fileID, file);

        log.debug("Created new File{} for: {} from User: {}", fileName, fileID, Long.toUnsignedString(srcUID));

        log.info("Starting with downloading of File: {} from User: {}", fileName, Long.toUnsignedString(srcUID));
        System.out.println("Starting with downloading of File: " + fileName + " from User: " + Long.toUnsignedString(srcUID));

    }

    private void handleFileData(BCPPacket packet) {
        log.debug("Received File Data");

        long srcUID = packet.getSrcNodeId();
        int fileId = packet.getFileId();
        int sequenz = packet.getSequenz();
        byte[] payload = packet.getPayload();


        File file = downloadFiles.getFile(srcUID, fileId);

        if(file != null){
            if (file.addChunk(payload, sequenz)) {
                log.debug("Added Chunk: {} to File: {}from User: {}", sequenz, file.getName(), Long.toUnsignedString(srcUID));
            }
            if(file.finished()){
                file.safeFile();
                file.stopRequesting();
                downloadFiles.removeFile(srcUID, fileId);
                log.info("Finished downloading File: {} from User: {}", file.getName(), Long.toUnsignedString(srcUID));
                System.out.println("Finished downloading File: " + file.getName() + " from User: " + Long.toUnsignedString(srcUID));
            }
        }
        else{
            log.debug("Failed to receive the file: {} from the user: {} ", fileId, srcUID);
        }
    }

    private void handleGoodbye(BCPPacket packet) {
        log.debug("Received Goodbye");

        long srcNodeId = packet.getSrcNodeId();
        routingTable.removeUIDThroughGoodbye(srcNodeId);


        log.debug("UID: {} removed", Long.toUnsignedString(srcNodeId));
        log.info("User: {} left the Chat", Long.toUnsignedString(srcNodeId));

        System.out.println("User: " + Long.toUnsignedString(srcNodeId) + " left the Chat");
    }

    private void handleWelcome(BCPPacket packet) {
        log.debug("Received Welcome");

        long srcNodeId = packet.getSrcNodeId();
        InetAddress srcAdress = packet.getAddress();
        int srcPort = packet.getPort();
        byte hops = packet.getHops();
        long lastSeen = System.currentTimeMillis();

        RoutingEntry entry = new RoutingEntryImpl(
                packet.getSrcNodeId(),
                packet.getAddress(),
                packet.getPort(),
                (byte)(packet.getHops() + 1),
                System.currentTimeMillis()
        );

        routingTable.add(entry);

        log.debug("Routing Entry added for {}", Long.toUnsignedString(srcNodeId));
        log.info("User: {} is available for Chatting", Long.toUnsignedString(srcNodeId));
        System.out.println("User: " + Long.toUnsignedString(srcNodeId) + " is available for Chatting");
    }

    private void handleHello(BCPPacket packet) {
        log.debug("Received Hello");

        long srcNodeId = packet.getSrcNodeId();
        InetAddress srcAddress = packet.getAddress();
        int srcPort = packet.getPort();
        byte hops = packet.getHops();
        long last_seen = System.currentTimeMillis();

        RoutingEntry entry = new RoutingEntryImpl(srcNodeId, srcAddress, srcPort, ++hops, last_seen);
        routingTable.add(entry);

        packet.setType(PacketTypes.WELCOME);
        packet.setHops((byte)0);
        packet.setTtl((byte) 32);
        packet.setSrcNodeId(storage.getID());
        packet.setDestNodeId(srcNodeId);
        packet.setFileId(0);
        packet.setSequenz(0);
        packet.setPayloadLength((short)0);
        packet.setPayload(new byte[0]);

        DatagramPacket welcomePacket = packet.makeDatagramPacket();

        senderQueue.add(welcomePacket);

        log.debug("Send Welcome packet to: {}" , Long.toUnsignedString(srcNodeId));

        log.info("User {} joined the Chat", Long.toUnsignedString(srcNodeId));

        System.out.println("User: " + Long.toUnsignedString(srcNodeId) + " joined the Chat");
    }

    private void handleFeedForwading(BCPPacket packet){
        log.debug("Start with feed forwarding");

        packet.dekrementTtl();
        long destId = packet.getDestNodeId();
        InetAddress nextHopAddress = routingTable.getNextHopAdressForUID(destId);
        int nextHopPort = routingTable.getNextHopPortForUID(destId);

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
