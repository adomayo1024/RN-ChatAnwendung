package ChatAnwendung.Impl.Handler.RecieverHandlers;

import java.net.DatagramPacket;

public class FileEndRecieveHandler extends AbstractRecieveHanlder{

    public FileEndRecieveHandler( DatagramPacket packet) {
        super(FileInitRecieveHandler.class.getName(), packet);
    }
}
