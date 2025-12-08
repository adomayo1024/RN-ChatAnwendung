package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.RecieverHanlder;
import ChatAnwendung.Impl.Handler.HandlerFactory;
import ChatAnwendung.Impl.SendMode;
import ChatAnwendung.Impl.Storage;

import java.net.DatagramPacket;
import java.util.concurrent.CompletableFuture;

public class RecieverHandlerImpl implements RecieverHanlder {

    @Override
    public void handle(DatagramPacket packet) {

        if(Storage.getInstance().isLogin() || Storage.getInstance().getSendMode() == SendMode.SELF){
            CompletableFuture.runAsync(HandlerFactory.getRecieverHandler(packet), Storage.getInstance().getThreadPool());
        }
    }
}
