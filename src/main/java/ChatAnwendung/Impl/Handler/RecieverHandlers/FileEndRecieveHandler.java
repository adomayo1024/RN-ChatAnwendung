package ChatAnwendung.Impl.Handler.RecieverHandlers;

import java.net.DatagramPacket;
import java.util.logging.Level;

public class FileEndRecieveHandler extends AbstractRecieveHanlder{

    public FileEndRecieveHandler( DatagramPacket packet) {
        super(FileInitRecieveHandler.class.getName(), packet);
    }

    @Override
    public void run(){
        byte[] data = packet.getData();
        int fileID = getFileId(data);
        long srcUID = getSrcUID(data);
        logger.log(Level.INFO, "Recieved File End from file: " + Long.toUnsignedString(fileID) + " from User: " + srcUID );
    }
}
