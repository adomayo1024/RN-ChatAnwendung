package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Handler.RecieverHandlers.AbstractRecieveHanlder;

import java.net.DatagramPacket;

public class FileDataRecieveHandler extends AbstractRecieveHanlder {
    public FileDataRecieveHandler(DatagramPacket packet) {
        super(FileDataRecieveHandler.class.getName(), packet);
    }
}
