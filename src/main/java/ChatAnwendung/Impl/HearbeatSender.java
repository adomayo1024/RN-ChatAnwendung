package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

@Slf4j
public class HearbeatSender extends AbstractHandler {

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
