package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.RoutingEntryImpl;
import ChatAnwendung.Impl.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;

@Slf4j
public class WelcomeRecieveHandler extends AbstractRecieveHanlder {
    public WelcomeRecieveHandler(DatagramPacket packet) {
        super(packet);
    }



    @Override
    public void run(){

        log.debug("Received Welcome");

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

        log.debug("Routing Entry added for {}", Long.toUnsignedString(srcUID));
    }

}
