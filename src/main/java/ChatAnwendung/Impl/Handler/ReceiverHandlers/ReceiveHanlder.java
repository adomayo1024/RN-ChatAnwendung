package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.BCPPacket;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class ReceiveHanlder extends AbstractHandler implements Runnable {


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



            switch (bcpPacket.getType()){
                case PacketTypes.HELLO -> handleHello(bcpPacket);

                case PacketTypes.WELCOME -> handleWelcome(bcpPacket);

                case PacketTypes.GOODBYE -> handleGoodbye(bcpPacket);

                case PacketTypes.FILE_INIT -> handleFileInit(bcpPacket);

                case PacketTypes.FILE_DATA -> handleFileData(bcpPacket);

                case PacketTypes.File_End -> handleFileEnd(bcpPacket);

                case PacketTypes.RESENDREQUEST -> handleResendRequest(bcpPacket);

                case PacketTypes.MESSAGE -> handleMessage(bcpPacket);

                case PacketTypes.HEARTBEAT -> handleHeartbeat(bcpPacket);

                case PacketTypes.ROUTINGTABLE -> handleRoutingTable(bcpPacket);
            }
        }


    }

    private void handleRoutingTable(BCPPacket packet) {
    }

    private void handleHeartbeat(BCPPacket packet) {
    }

    private void handleMessage(BCPPacket packet) {
    }

    private void handleResendRequest(BCPPacket packet) {
    }

    private void handleFileEnd(BCPPacket packet) {

    }

    private void handleFileInit(BCPPacket packet) {
        log.debug("Received File Init");

        int anzahlChunks = packet.getSequenz();
        int fileID = packet.getFileId();
        long srcUID = packet.getSrcNodeId();
        byte[] payload = packet.getPayload();
        short payloadLength = packet.getPayloadLength();
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

        file.startRequesting();

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

        RoutingEntry entry = new RoutingEntryImpl(srcNodeId, srcAddress, srcPort, hops, last_seen);
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

    private String getFileName(byte[] payload, short payloadLength){
        byte[] name = new byte[payloadLength - 4];

        for(int i = 0; i < name.length; i++){
            name[i] = payload[i + 4];
        }

        return new String(name, StandardCharsets.UTF_8);
    }

}
