package ChatAnwendung.Impl.Exceptions;

public class ArgumentException extends Throwable {

    private final String msg;

    public ArgumentException(String message) {
        msg = message;

    }

    @Override
    public String getMessage() {
        return "Argument Error: " + msg;
    }
}
