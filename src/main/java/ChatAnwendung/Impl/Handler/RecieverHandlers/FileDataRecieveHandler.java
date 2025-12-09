package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.DownloadFiles;
import ChatAnwendung.Impl.File;
import ChatAnwendung.Impl.Handler.Header;

import java.net.DatagramPacket;
import java.util.logging.Level;

public class FileDataRecieveHandler extends AbstractRecieveHanlder {
    public FileDataRecieveHandler(DatagramPacket packet) {
        super(FileDataRecieveHandler.class.getName(), packet);
    }


    @Override
    public void run(){
        byte[] data = packet.getData();
        long srcUID = getSrcUID(data);
        int fileId = getFileId(data);
        int sequenz = getSequenz(data);
        short payloadLength = getPayloadLength(data);
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, Header.getPayloadPos(), payload, 0, payloadLength);
        File file = DownloadFiles.getInstance().getFile(srcUID, fileId);
        file.setSequenzGetted(sequenz);
        if(file.addChunk(payload, sequenz)){
            DownloadFiles.getInstance().removeFile(srcUID, fileId);
        }

        logger.log(Level.INFO, "Added Chunk: " + sequenz + "to File: " + file.getName() + "from User: " + Long.toUnsignedString(srcUID));

    }
}
