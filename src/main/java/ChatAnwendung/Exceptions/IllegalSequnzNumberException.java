package ChatAnwendung.Exceptions;

public class IllegalSequnzNumberException extends Throwable {

    private final Object sequenz;

    public IllegalSequnzNumberException(long sequenz) {
        this.sequenz = sequenz;
    }
}
