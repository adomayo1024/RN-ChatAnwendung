package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Impl.Handler.HandlerFactory;
import ChatAnwendung.Impl.Storage;
import ChatAnwendung.Impl.ThreadPools;

import java.util.concurrent.CompletableFuture;

public class InputHandlerImple implements InputHandler {
    @Override
    public void handle(String stdIn) {
        if(!Storage.getInstance().isLogin()) {
            String[] string = stdIn.split(" ");
            if (!string[0].equals("hello") && !string[0].equals("help") && !string[0].equals("exit") && !string[0].equals("connect")){
                stdIn = "";
            }
        }

        Handler handler = HandlerFactory.getInputHandler(stdIn);
        CompletableFuture.runAsync(handler, ThreadPools.getInstance().getThreadPool());
    }
}
