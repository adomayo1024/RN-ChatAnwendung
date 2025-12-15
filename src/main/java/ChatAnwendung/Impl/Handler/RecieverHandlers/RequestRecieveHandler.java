package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.Exceptions.IllegalSequnzNumberException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.Storage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class RequestRecieveHandler extends AbstractRecieveHanlder {
    public RequestRecieveHandler(DatagramPacket packet) {
        super(RequestRecieveHandler.class.getName(), packet);
    }

    @Override
    public void run() {
        byte[] data = packet.getData();
        int sequenz = getSequenz(data);
        int fileId = getFileId(data);
        String filePath = Storage.getInstance().getOpenFile(fileId);
        long srcUID = getSrcUID(data);
        InetAddress srcAddress = packet.getAddress();
        int srcPort = packet.getPort();


        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            byte[] payload = split(file, sequenz);

            DatagramPacket requestPacket = makeDatagramPackage(
                    PacketTypes.FILE_DATA,
                    srcUID,
                    sequenz,
                    fileId,
                    payload,
                    srcAddress,
                    srcPort);
            MessageQueue.getInstance().push(requestPacket);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private byte[] split(RandomAccessFile file, long sequenz){
        byte[] chunk = null;

        try {
            long anzahlChunks = (long)Math.ceil(file.length() / 1300.0);
            if(anzahlChunks <= sequenz || sequenz < 0){
                throw new IllegalSequnzNumberException(sequenz);
            }
            else if(anzahlChunks - 1 == sequenz){
                int size = (int)(file.length() % 1300);
                chunk = new byte[size];
            }
            else{
                chunk = new byte[1300];
            }
            file.seek(sequenz);
            file.read(chunk);
        } catch (IOException | IllegalSequnzNumberException e) {
            ExceptionHandler.handle(e, this.getClass());
        }
        return chunk;
    }
}
