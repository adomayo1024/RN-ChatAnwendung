package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.DownloadFiles;
import ChatAnwendung.Impl.File;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.logging.Level;

@Slf4j
public class FileEndRecieveHandler extends AbstractRecieveHanlder{

    public FileEndRecieveHandler( DatagramPacket packet) {
        super(packet);
    }

    @Override
    public void run(){
        byte[] data = packet.getData();
        int fileID = getFileId(data);
        long srcUID = getSrcUID(data);
        log.info( "Recieved File End from file: " + fileID + " from User: " + Long.toUnsignedString(srcUID));
        File file = DownloadFiles.getInstance().getFile(srcUID, fileID);
        file.startRequesting();
    }
}
