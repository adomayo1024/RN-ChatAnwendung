package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.BCPPacket;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.RoutingEntryImpl;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;

@Slf4j
public class HelloRecieveHandler extends AbstractRecieveHanlder {
    public HelloRecieveHandler(DatagramPacket packet) {
        super(packet);
    }



    @Override
    public void run(){

        log.debug("Received Hello");

        byte[] data = packet.getData();
        long srcUID = getSrcUID(data);
        InetAddress srcAdress = packet.getAddress();
        int srcPort = packet.getPort();
        byte hops = (byte) (data[BCPPacket.getHopsPos()] + 1);
        long last_seen = System.currentTimeMillis();

        RoutingEntry entry = new RoutingEntryImpl(srcUID, srcAdress, srcPort, hops, last_seen);
        RoutingTableImpl.getInstance().add(entry);

        byte[] welcomePayload = new byte[0];

        DatagramPacket welcomePacket = makeDatagramPackage(
                PacketTypes.WELCOME,
                srcUID,
                0,
                0,
                welcomePayload,
                srcAdress,
                srcPort);

        MessageQueue.getInstance().push(welcomePacket);

        log.debug("Send Welcome packet to: {}" , Long.toUnsignedString(srcUID));

        log.info("User {} joined the Chat", Long.toUnsignedString(srcUID));

        System.out.println("User: " + Long.toUnsignedString(srcUID) + " joined the Chat");
    }
}
