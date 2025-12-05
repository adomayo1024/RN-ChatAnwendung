package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;

import java.net.DatagramPacket;

public class FileInitRecieveHandler extends AbstractRecieveHanlder {


    public FileInitRecieveHandler(DatagramPacket packet) {
        super(FileInitRecieveHandler.class.getName(), packet);
    }
}
