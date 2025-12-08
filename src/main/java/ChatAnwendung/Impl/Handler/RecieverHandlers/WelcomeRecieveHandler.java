package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;

import java.net.DatagramPacket;

public class WelcomeRecieveHandler extends AbstractRecieveHanlder {
    public WelcomeRecieveHandler(DatagramPacket packet) {
        super(WelcomeRecieveHandler.class.getName(), packet);
    }



}
