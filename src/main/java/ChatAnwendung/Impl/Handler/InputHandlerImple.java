package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Impl.Storage;

import java.util.concurrent.CompletableFuture;

public class InputHandlerImple implements InputHandler {
    @Override
    public void handle(String stdIn) {
        if(!Storage.getInstance().isLogin()) {
            String[] string = stdIn.split(" ");
            if (!string[0].equals("hello") && !string[0].equals("help") && !string[0].equals("exit")){
                stdIn = "";
            }
        }

        Handler handler = HandlerFactory.getHandler(stdIn);
        CompletableFuture.runAsync(handler, Storage.getInstance().getThreadPool());
    }
}
