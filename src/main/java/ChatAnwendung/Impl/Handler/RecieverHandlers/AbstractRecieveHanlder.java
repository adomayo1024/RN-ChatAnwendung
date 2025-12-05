package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.Handler.AbstractHandler;

import java.net.DatagramPacket;

public class AbstractRecieveHanlder extends AbstractHandler {

    private final DatagramPacket  packet;

    protected AbstractRecieveHanlder(String name, DatagramPacket packet) {
        super(name);
        this.packet = packet;
    }
}
