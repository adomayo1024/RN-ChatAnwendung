package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ExitInputHandler extends AbstractInputHandler {

    public ExitInputHandler(String[] command) {
        super(command);
    }

    @Override
    public void run() {

        log.debug("Start with shutdown");

        try {
            if(Storage.getInstance().isLogin()){
                GoodbyeInputHandler bye = new GoodbyeInputHandler(new String[]{"bye"});
                bye.run();
            }
            Storage.getInstance().getReader().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.debug("Initialization finished");
    }


    public static String help(){

        return "exit: Meldet den User ab und beendet das Programm kommplett\n" +
                "\tAufbau: exit\n" +
                "\tFehler:\n";
    }
}
