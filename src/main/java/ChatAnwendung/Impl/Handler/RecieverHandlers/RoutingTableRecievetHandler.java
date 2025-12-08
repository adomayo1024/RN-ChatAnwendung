package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;

import java.net.DatagramPacket;

public class RoutingTableRecievetHandler extends AbstractRecieveHanlder {
    public RoutingTableRecievetHandler(DatagramPacket packet) {
        super(RoutingTableRecievetHandler.class.getName(), packet);
    }
}
