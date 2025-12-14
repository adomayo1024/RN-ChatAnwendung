package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.RecieverHanlder;
import ChatAnwendung.Impl.Handler.HandlerFactory;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.SendMode;
import ChatAnwendung.Impl.Storage;
import ChatAnwendung.Impl.ThreadPools;

import java.net.DatagramPacket;
import java.util.concurrent.CompletableFuture;

public class RecieverHandlerImpl implements RecieverHanlder {

    @Override
    public void handle(DatagramPacket packet) {

        boolean isItForMe = isItForMe(packet);

        if(Storage.getInstance().isLogin() && Storage.getInstance().getSendMode() == SendMode.SELF){
            CompletableFuture.runAsync(HandlerFactory.getRecieverHandler(packet, isItForMe), ThreadPools.getInstance().getThreadPool());
        }
    }

    private boolean isItForMe(DatagramPacket packet){
        if(Storage.getInstance().getSendMode() == SendMode.SELF) return true;
        long destUID = makeBytesToLong(packet.getData(), Header.getDestNodePos());


        return destUID == Storage.getInstance().getID();
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
}
