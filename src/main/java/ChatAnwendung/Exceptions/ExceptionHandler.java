package ChatAnwendung.Exceptions;

import ChatAnwendung.logic.Api.Handler;
import lombok.extern.slf4j.Slf4j;

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
        log.warn("{} hat Fehler gemeldet: {}", thrower.getName(), exception.getMessage());
    }

    public static void handle(Throwable exception, Class<?> thrower){
        log.warn("{} hat Fehler gemeldet: {}", thrower.getName(), exception.getMessage());
        System.out.println(exception.getMessage());
    }
}
