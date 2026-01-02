package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.File;
import ChatAnwendung.Impl.Header;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

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
        if(file != null){
            if (file.addChunk(payload, sequenz)) {
                log.debug("Added Chunk: {} to File: {}from User: {}", sequenz, file.getName(), Long.toUnsignedString(srcUID));
            }
            if(file.finished()){
                DownloadFiles.getInstance().removeFile(srcUID, fileId);
            }
        }
        else{
            log.debug("Failed to receive the file: {} from the user: {} ", file, srcUID);
        }

    }
}
