package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

@Slf4j
public class HeartbeatRecieveHandler extends AbstractRecieveHanlder {
    public HeartbeatRecieveHandler(DatagramPacket packet) {
        super(packet);
    }

    @Override
    public void run() {

        log.debug("Received Heartbeat");

        byte[] data = packet.getData();
        long srcUID = getSrcUID(data);
        RoutingTableImpl.getInstance().setLastSeen(srcUID);

        log.debug("Last seen set for {}", Long.toUnsignedString(srcUID));
    }
}
