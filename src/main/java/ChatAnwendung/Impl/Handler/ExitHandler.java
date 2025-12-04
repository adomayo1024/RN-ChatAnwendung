package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.Storage;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ExitHandler extends AbstractHandler {

    public ExitHandler(String[] command) {
        super(command, ExitHandler.class.getName());
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "beginnung mit schließung");
        try {
            if(Storage.getInstance().isLogin()){
                GoodbyeHandler bye = new GoodbyeHandler(new String[]{"bye"});
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
