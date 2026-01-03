package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Api.RecieverHanlder;
import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.Handler.Common.HandlerFactory;
import ChatAnwendung.Impl.persistence.Storage;
import ChatAnwendung.Impl.persistence.ThreadPools;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RecieverHandlerImpl implements RecieverHanlder {

    private final BlockingQueue<DatagramPacket> queue;

    public RecieverHandlerImpl(BlockingQueue<DatagramPacket> queue){
        this.queue = queue;

    }

    @Override
    public void handle(DatagramPacket packet) {

        log.debug("Start with handle the packet from the Type {}", PacketTypes.values()[packet.getData()[Header.getTypePos()]]);

        if(checksumRight(packet)){
            boolean isItForMe = isItForMe(packet);

            if(Storage.getInstance().isLogin()){
                CompletableFuture.runAsync(HandlerFactory.getRecieverHandler(packet, isItForMe), ThreadPools.getInstance().getThreadPool());
            }
        }
        else {
            log.debug("Checksum is wrong");
        }

        log.debug("Finished with handle the packet");
    }

    private boolean checksumRight(DatagramPacket packet) {
        byte[] data = packet.getData();
        long expectCrc = makeBytesToLong(data, Header.getCrcPos());
        long realCrc = Header.makeChecksum(Header.extractChecksum(data));

        return expectCrc == realCrc;
    }

    private boolean isItForMe(DatagramPacket packet){

        long destUID = makeBytesToLong(packet.getData(), Header.getDestNodePos());

        return destUID == Storage.getInstance().getID() || destUID == -1;
    }

    private long makeBytesToLong(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];
        byte b2 = data[pos + 2];
        byte b3 = data[pos + 3];
        byte b4 = data[pos + 4];
        byte b5 = data[pos + 5];
        byte b6 = data[pos + 6];
        byte b7 = data[pos + 7];

        return ((long)(b0 & 0xFF) << 56) |
                ((long)(b1 & 0xFF) << 48) |
                ((long)(b2 & 0xFF) << 40) |
                ((long)(b3 & 0xFF) << 32) |
                ((long)(b4 & 0xFF) << 24) |
                ((long)(b5 & 0xFF) << 16) |
                ((long)(b6 & 0xFF) <<  8) |
                ((long)(b7 & 0xFF));
    }

    @Override
    public void run() {

        boolean interrupted = false;

        while(!interrupted){
            try {
                handle(queue.take());
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        log.debug("ReceiveHandler shutdown");
    }
}
