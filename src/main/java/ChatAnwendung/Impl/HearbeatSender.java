package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;

import java.net.DatagramPacket;

public class HearbeatSender extends AbstractHandler {


    public HearbeatSender() {
        super(HearbeatSender.class.getName());
    }

    @Override
    public void run() {
        if(Storage.getInstance().isLogin()){
            for(RoutingEntry entry : RoutingTableImpl.getInstance().getAllDirectNeighbours()){
                if(entry.isRoutable()){
                    byte[] payload = new byte[0];
                    DatagramPacket packet = makeDatagramPackage(
                            PacketTypes.HEARTBEAT,
                            (byte)1,
                            entry.getUID(),
                            0,
                            0,
                            payload,
                            entry.getNextHopAdress(),
                            entry.getNextHopPort());
                    MessageQueue.getInstance().push(packet);
                }
            }
        }
    }
}
