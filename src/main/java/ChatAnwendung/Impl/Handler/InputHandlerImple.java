package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Impl.Storage;

import java.util.concurrent.CompletableFuture;

public class InputHandlerImple implements InputHandler {
    @Override
    public void handle(String stdIn) {
        CompletableFuture.runAsync(HandlerFactory.getHandler(stdIn), Storage.getInstance().getThreadPool());
    }
}
