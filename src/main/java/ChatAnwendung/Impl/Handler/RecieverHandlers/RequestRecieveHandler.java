package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;

import java.net.DatagramPacket;

public class RequestRecieveHandler extends AbstractRecieveHanlder {
    public RequestRecieveHandler(DatagramPacket packet) {


        super(RequestRecieveHandler.class.getName(), packet);
    }
}
