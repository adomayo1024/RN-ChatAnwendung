package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandler implements Handler {

    private final Throwable exception;

    private final Class<?> thrower;

    private static final  Logger logger = Logger.getLogger(ExceptionHandler.class.getName());

    public ExceptionHandler(Throwable e, Class<?> t) {
        exception = e;
        thrower = t;
    }

    @Override
    public void run() {
        logger.log(Level.WARNING, thrower.getName() + " hat Fehler gemeldet: " +  exception.getMessage());
    }

    public static void handle(Throwable exception, Class<?> thrower){
        logger.log(Level.WARNING, thrower.getName() + " hat Fehler gemeldet: " +  exception.getMessage());
    }
}
