package ChatAnwendung.Exceptions;

public class InvalidMessageException extends Throwable {
    public InvalidMessageException(String msg) {
        super(msg);
    }
}
