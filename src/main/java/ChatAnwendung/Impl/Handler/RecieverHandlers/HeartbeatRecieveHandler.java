package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.RoutingTableImpl;

import java.net.DatagramPacket;

public class HeartbeatRecieveHandler extends AbstractRecieveHanlder {
    public HeartbeatRecieveHandler(DatagramPacket packet) {
        super(HeartbeatRecieveHandler.class.getName(), packet);
    }

    @Override
    public void run() {
        byte[] data = packet.getData();
        long srcUID = getSrcUID(data);
        RoutingTableImpl.getInstance().setLastSeen(srcUID);
    }
}
