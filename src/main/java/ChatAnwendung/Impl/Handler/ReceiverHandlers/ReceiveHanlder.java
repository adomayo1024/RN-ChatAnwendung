package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.BCPPacket;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.RoutingEntryImpl;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import ChatAnwendung.Impl.persistence.Storage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class ReceiveHanlder extends AbstractHandler implements Runnable {


    private final BlockingQueue<DatagramPacket> receiverQueue;

    private final BlockingQueue<DatagramPacket> senderQueue;

    private final RoutingTable routingTable;

    private final Storage storage;

    private boolean interrupted;

    public ReceiveHanlder(BlockingQueue<DatagramPacket> receiverQueue, BlockingQueue<DatagramPacket> senderQueue, RoutingTable routingTable, Storage storage){
        this.receiverQueue = receiverQueue;
        this.senderQueue = senderQueue;
        this.routingTable = routingTable;
        this.storage = storage;
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
    }

    private void handleFileData(BCPPacket packet) {

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

}
