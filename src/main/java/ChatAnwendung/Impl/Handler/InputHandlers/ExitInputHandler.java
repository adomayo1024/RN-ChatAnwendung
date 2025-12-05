package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Storage;

import java.io.IOException;
import java.util.logging.Level;

public class ExitInputHandler extends AbstractInputHandler {

    public ExitInputHandler(String[] command) {
        super(command, ExitInputHandler.class.getName());
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "beginnung mit schließung");
        try {
            if(Storage.getInstance().isLogin()){
                GoodbyeInputHandler bye = new GoodbyeInputHandler(new String[]{"bye"});
                bye.run();
            }
            logger.log(Level.INFO,  "Werde schließen");
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
