package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.RoutingEntryImpl;
import ChatAnwendung.Impl.RoutingTableImpl;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class WelcomeRecieveHandler extends AbstractRecieveHanlder {
    public WelcomeRecieveHandler(DatagramPacket packet) {
        super(WelcomeRecieveHandler.class.getName(), packet);
    }



    @Override
    public void run(){
        byte[] data = packet.getData();
        long srcUID = getSrcUID(data);
        InetAddress srcAdress = packet.getAddress();
        int srcPort = packet.getPort();
        byte hops = data[Header.getHopsPos()];
        long lastSeen = System.currentTimeMillis();

        RoutingEntry entry = new RoutingEntryImpl(
                srcUID,
                srcAdress,
                srcPort,
                hops + 1,
                lastSeen
        );

        RoutingTableImpl.getInstance().add(entry);
    }

}
