package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Storage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.logging.Level;

@Slf4j
public class ExitInputHandler extends AbstractInputHandler {

    public ExitInputHandler(String[] command) {
        super(command);
    }

    @Override
    public void run() {
        log.debug( "beginnung mit schließung");
        try {
            if(Storage.getInstance().isLogin()){
                GoodbyeInputHandler bye = new GoodbyeInputHandler(new String[]{"bye"});
                bye.run();
            }
            log.debug(  "Werde schließen");
            Storage.getInstance().getReader().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static String help(){

        return "exit: Meldet den User ab und beendet das Programm kommplett\n" +
                "\tAufbau: exit\n" +
                "\tFehler:\n";
    }
}
