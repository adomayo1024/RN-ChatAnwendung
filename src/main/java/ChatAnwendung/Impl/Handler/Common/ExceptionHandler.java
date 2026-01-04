package ChatAnwendung.Impl.Handler.Common;

import ChatAnwendung.Api.Handler;
import lombok.extern.slf4j.Slf4j;

import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class ExceptionHandler implements Handler {

    private final Throwable exception;

    private final Class<?> thrower;

    public ExceptionHandler(Throwable e, Class<?> t) {
        exception = e;
        thrower = t;
    }

    @Override
    public void run() {
        log.warn( thrower.getName() + " hat Fehler gemeldet: " +  exception.getMessage());
    }

    public static void handle(Throwable exception, Class<?> thrower){
        log.warn( thrower.getName() + " hat Fehler gemeldet: " +  exception.getMessage());
        System.out.println(exception.getMessage());
    }
}
