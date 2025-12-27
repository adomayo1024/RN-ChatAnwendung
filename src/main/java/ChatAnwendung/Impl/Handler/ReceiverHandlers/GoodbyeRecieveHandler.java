package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

@Slf4j
public class GoodbyeRecieveHandler extends AbstractRecieveHanlder {
    public GoodbyeRecieveHandler(DatagramPacket packet) {
        super(packet);
    }


    @Override
    public void run(){

        log.debug("Received Goodbye");

        long srcUID = getSrcUID(packet.getData());
        RoutingTableImpl.getInstance().removeUIDThroughGoodbye(srcUID);
        log.debug("UID: {} removed", Long.toUnsignedString(srcUID));

    }
}
