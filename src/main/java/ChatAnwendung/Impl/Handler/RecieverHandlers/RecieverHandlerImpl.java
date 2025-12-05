package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Api.RecieverHanlder;
import ChatAnwendung.Impl.Handler.InputHandlers.HandlerFactory;

import java.net.DatagramPacket;
import java.util.concurrent.CompletableFuture;

public class RecieverHandlerImpl implements RecieverHanlder {

    @Override
    public void hanlde(DatagramPacket packet) {
        CompletableFuture.runAsync(HandlerFactory.getRecieverHandler(packet));
    }
}
