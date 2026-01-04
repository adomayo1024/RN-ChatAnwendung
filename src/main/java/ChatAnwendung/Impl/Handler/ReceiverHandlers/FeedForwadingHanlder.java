package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;

@Slf4j
public class FeedForwadingHanlder extends AbstractRecieveHanlder{
    public FeedForwadingHanlder(DatagramPacket packet) {
        super(packet);
    }

    @Override
    public void run(){

        log.debug("Start with feed forwarding");

        byte[] data = packet.getData();
        byte ttl = getTtl(data);
        long srcId = getSrcUID(data);
        ttl -= 1;
        InetAddress nextHopAddress = RoutingTableImpl.getInstance().getNextHopAdressForUID(srcId);
        int nextHopPort = RoutingTableImpl.getInstance().getNextHopPortForUID(srcId);

        if(ttl > 0 || nextHopAddress == null || nextHopPort == -1){
            byte hops = getHops(data);
            hops += 1;
            data[Header.getTtlPos()] = ttl;
            data[Header.getHopsPos()] = hops;
            long newChecksum = Header.makeChecksum(Header.extractChecksum(data));
            Header.addLong(Header.getCrcPos(), newChecksum, data);
            packet.setAddress(nextHopAddress);
            packet.setPort(nextHopPort);
            MessageQueue.getInstance().push(packet);

            log.debug("Packet forwarded");
        }
        else {
            log.debug("Packet throw away from: {}", srcId);
        }
    }
}
