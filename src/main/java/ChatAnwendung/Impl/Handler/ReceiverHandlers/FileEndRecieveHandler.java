package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Impl.DownloadFiles;
import ChatAnwendung.Impl.File;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

@Slf4j
public class FileEndRecieveHandler extends AbstractRecieveHanlder{

    public FileEndRecieveHandler( DatagramPacket packet) {
        super(packet);
    }

    @Override
    public void run(){

        log.debug("Received File End");

        byte[] data = packet.getData();
        int fileID = getFileId(data);
        long srcUID = getSrcUID(data);
        log.debug( "Recieved File End from file: " + fileID + " from User: " + Long.toUnsignedString(srcUID));
        File file = DownloadFiles.getInstance().getFile(srcUID, fileID);
        file.startRequesting();

        log.debug("Started requesting File: {}", fileID);
    }
}
