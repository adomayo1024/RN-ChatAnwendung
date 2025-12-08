package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandler implements Handler {

    private final Throwable exception;

    private final Class<?> thrower;

    private final  Logger logger;

    public ExceptionHandler(Throwable e, Class<?> t) {
        exception = e;
        thrower = t;
        logger = Logger.getLogger(ExceptionHandler.class.getName());
    }

    @Override
    public void run() {
        logger.log(Level.WARNING, thrower.getName() + " hat Fehler gemeldet: " +  exception.getMessage());
    }
}
