package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Handler.RecieverHandlers.AbstractRecieveHanlder;

import java.net.DatagramPacket;

public class MessageRecieveHandler extends AbstractRecieveHanlder {
    public MessageRecieveHandler(DatagramPacket packet) {
        super(MessageRecieveHandler.class.getName(), packet);
    }
}
