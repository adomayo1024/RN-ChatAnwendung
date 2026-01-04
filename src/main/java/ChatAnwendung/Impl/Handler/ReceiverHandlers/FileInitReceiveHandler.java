package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.File;
import ChatAnwendung.Impl.Header;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
public class FileInitReceiveHandler extends AbstractRecieveHanlder {


    public FileInitReceiveHandler(DatagramPacket packet) {
        super(packet);
    }

    @Override
    public void run(){

        log.debug("Received File Init");

        byte[] data = packet.getData();
        int anzahlChunks = getSequenz(data);
        int fileID = getFileId(data);
        long srcUID = getSrcUID(data);
        short payloadLength = getPayloadLength(data);
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, Header.getPayloadPos(), payload, 0, payloadLength);
        String fileName = getFileName(payload, payloadLength);
        int size = getSize(payload);
        ScheduledExecutorService timer = DownloadFiles.getInstance().getScheduledThreadPool();

        File file = new File(
                anzahlChunks,
                size,
                fileName,
                fileID,
                srcUID,
                timer);

        DownloadFiles.getInstance().setNewFile(srcUID, fileID, file);

        file.startRequesting();

        log.debug("Created new File{} for: {} from User: {}", fileName, fileID, Long.toUnsignedString(srcUID));

        log.info("Starting with downloading of File: {} from User: {}", fileName, Long.toUnsignedString(srcUID));
        System.out.println("Starting with downloading of File: " + fileName + " from User: " + Long.toUnsignedString(srcUID));

    }


    private String getFileName(byte[] payload, short payloadLength){
        byte[] name = new byte[payloadLength - 4];

        for(int i = 0; i < name.length; i++){
            name[i] = payload[i + 4];
        }

        return new String(name, StandardCharsets.UTF_8);
    }

    private int getSize(byte[] payload) {
        return makeBytesToInt(payload, 0);
    }
}
