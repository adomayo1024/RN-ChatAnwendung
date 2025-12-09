package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.DownloadFiles;
import ChatAnwendung.Impl.File;
import ChatAnwendung.Impl.Handler.HandlerFactory;
import ChatAnwendung.Impl.Handler.Header;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;

public class FileInitRecieveHandler extends AbstractRecieveHanlder {


    public FileInitRecieveHandler(DatagramPacket packet) {
        super(FileInitRecieveHandler.class.getName(), packet);
    }

    @Override
    public void run(){
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

        logger.log(Level.INFO, "Created new File" + fileName +  " for: " + fileID + " from User: " + Long.toUnsignedString(srcUID));

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
