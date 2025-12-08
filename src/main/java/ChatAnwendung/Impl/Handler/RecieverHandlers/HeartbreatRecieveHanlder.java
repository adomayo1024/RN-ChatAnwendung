package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;

import java.net.DatagramPacket;

public class HeartbreatRecieveHanlder extends AbstractRecieveHanlder {
    public HeartbreatRecieveHanlder(DatagramPacket packet) {
        super(HeartbreatRecieveHanlder.class.getName(), packet);
    }
}
