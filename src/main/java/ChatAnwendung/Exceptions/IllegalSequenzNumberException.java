package ChatAnwendung.Exceptions;

public class IllegalSequenzNumberException extends Throwable {

    private final Object sequenz;

    public IllegalSequenzNumberException(long sequenz) {
        this.sequenz = sequenz;
    }
}
