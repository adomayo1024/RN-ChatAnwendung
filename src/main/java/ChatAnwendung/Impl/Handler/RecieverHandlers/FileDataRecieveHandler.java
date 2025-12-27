package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.DownloadFiles;
import ChatAnwendung.Impl.File;
import ChatAnwendung.Impl.Header;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.logging.Level;

@Slf4j
public class FileDataRecieveHandler extends AbstractRecieveHanlder {
    public FileDataRecieveHandler(DatagramPacket packet) {
        super(packet);
    }


    @Override
    public void run(){

        log.debug("Received File Data");

        byte[] data = packet.getData();
        long srcUID = getSrcUID(data);
        int fileId = getFileId(data);
        int sequenz = getSequenz(data);
        short payloadLength = getPayloadLength(data);
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, Header.getPayloadPos(), payload, 0, payloadLength);
        File file = DownloadFiles.getInstance().getFile(srcUID, fileId);
        if(file.addChunk(payload, sequenz)){
            DownloadFiles.getInstance().removeFile(srcUID, fileId);
        }

        log.debug("Added Chunk: {}to File: {}from User: {}", sequenz, file.getName(), Long.toUnsignedString(srcUID));

    }
}
