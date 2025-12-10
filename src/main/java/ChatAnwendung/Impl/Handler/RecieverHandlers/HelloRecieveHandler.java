package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.RoutingEntryImpl;
import ChatAnwendung.Impl.RoutingTableImpl;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class HelloRecieveHandler extends AbstractRecieveHanlder {
    public HelloRecieveHandler(DatagramPacket packet) {
        super(HelloRecieveHandler.class.getName(), packet);
    }



    @Override
    public void run(){
        byte[] data = packet.getData();
        long srcUID = getSrcUID(data);
        InetAddress srcAdress = packet.getAddress();
        int srcPort = packet.getPort();
        byte hops = (byte) (data[Header.getHopsPos()] + 1);
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
    }
}
