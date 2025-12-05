package ChatAnwendung.Impl.Handler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExceptionHandler implements Runnable {

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
        logger.log(Level.WARNING, exception.toString());
    }
}
