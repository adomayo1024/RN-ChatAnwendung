package ChatAnwendung.Impl.Handler.RecieverHandlers;

import java.net.DatagramPacket;

public class HeartbeatRecieveHandler extends AbstractRecieveHanlder {
    public HeartbeatRecieveHandler(DatagramPacket packet) {
        super(HeartbeatRecieveHandler.class.getName(), packet);
    }
}
