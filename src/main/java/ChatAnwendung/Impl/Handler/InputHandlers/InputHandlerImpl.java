package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Impl.Handler.Common.HandlerFactory;
import ChatAnwendung.Impl.Storage;
import ChatAnwendung.Impl.ThreadPools;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class InputHandlerImpl implements InputHandler {

    private Set<String> isLougoutCommand;

    public InputHandlerImpl() {
        this.isLougoutCommand = new HashSet<>(){{add("hello");add("help");add("exit");add("connect");add("disconnect");add("list");}};
    }

    @Override
    public void handle(String stdIn) {
        if(!Storage.getInstance().isLogin()) {
            String[] string = stdIn.split(" ");
            if (!isLogoutCommand(string[0])){
                stdIn = "";
            }
        }

        Handler handler = HandlerFactory.getInputHandler(stdIn);
        CompletableFuture.runAsync(handler, ThreadPools.getInstance().getThreadPool());
    }


    private boolean isLogoutCommand(String command){
        return this.isLougoutCommand.contains(command);
    }
}
