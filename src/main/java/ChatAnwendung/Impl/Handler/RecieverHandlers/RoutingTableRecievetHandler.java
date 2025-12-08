package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Handler.Header;
import ChatAnwendung.Impl.RoutingEntryImpl;
import ChatAnwendung.Impl.RoutingTableImpl;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class RoutingTableRecievetHandler extends AbstractRecieveHanlder {

    private final int routingEntrySize = 17;

    public RoutingTableRecievetHandler(DatagramPacket packet) {
        super(RoutingTableRecievetHandler.class.getName(), packet);
    }

    @Override
    public void run(){
        byte[] data = packet.getData();
        InetAddress srcAdress = packet.getAddress();
        int srcPort = packet.getPort();
        int payloadLength = getPayloadLength(data);
        int anzahlEintraege = payloadLength / routingEntrySize;
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, 38, payload, 0, payloadLength);

        for(int offset = 0; offset < payloadLength; offset += routingEntrySize){
            long uID = makeBytesToLong(payload, offset);
            byte hops = payload[8 + offset];
            long lastSeen = makeBytesToLong(payload, 9 + offset);

            RoutingEntry entry = new RoutingEntryImpl(uID, srcAdress, srcPort, hops + 1, lastSeen);
            RoutingTableImpl.getInstance().add(entry);
        }
    }




}
