package ChatAnwendung.Impl.Exceptions;

public class UnknowUIDException extends Throwable {


    private final Long uID;

    public UnknowUIDException(Long uID) {
        this.uID = uID;
    }
}
