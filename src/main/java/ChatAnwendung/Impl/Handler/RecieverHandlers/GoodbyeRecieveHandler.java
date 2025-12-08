package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.RoutingTableImpl;

import java.net.DatagramPacket;
import java.util.logging.Level;

public class GoodbyeRecieveHandler extends AbstractRecieveHanlder {
    public GoodbyeRecieveHandler(DatagramPacket packet) {
        super(GoodbyeRecieveHandler.class.getName(), packet);
    }


    @Override
    public void run(){

        long srcUID = getSrcUID(packet.getData());
        RoutingTableImpl.getInstance().removeUIDThroughGoodbye(srcUID);
        logger.log(Level.INFO, "UID: " + Long.toUnsignedString(srcUID) + " removed");

    }
}
