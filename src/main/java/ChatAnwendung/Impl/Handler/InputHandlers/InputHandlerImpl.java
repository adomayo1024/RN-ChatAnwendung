package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Api.InputHandler;
import ChatAnwendung.Impl.Handler.Common.HandlerFactory;
import ChatAnwendung.Impl.persistence.Storage;
import ChatAnwendung.Impl.persistence.ThreadPools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class InputHandlerImpl implements InputHandler {

    private Set<String> isLougoutCommand;

    public InputHandlerImpl() {
        this.isLougoutCommand = new HashSet<>(){{add("hello");add("help");add("exit");add("connect");add("disconnect");add("list");}};
    }

    @Override
    public void handle(String stdIn) {

        log.debug("Handle input: " + stdIn);

        if(!Storage.getInstance().isLogin()) {
            String[] string = stdIn.split(" ");
            if (!isLogoutCommand(string[0])){
                stdIn = "";
            }
        }

        Handler handler = HandlerFactory.getInputHandler(stdIn);
        CompletableFuture.runAsync(handler, ThreadPools.getInstance().getThreadPool());

        log.debug("Finished with input handling");
    }


    private boolean isLogoutCommand(String command){
        return this.isLougoutCommand.contains(command);
    }
}
