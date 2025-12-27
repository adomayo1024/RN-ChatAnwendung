package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.logging.Level;

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
