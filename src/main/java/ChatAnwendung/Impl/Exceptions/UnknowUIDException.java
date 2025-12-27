package ChatAnwendung.Impl.Exceptions;

public class UnknowUIDException extends Throwable {


    private final Long uID;

    public UnknowUIDException(Long uID) {
        this.uID = uID;
    }

    @Override
    public String getMessage() {
        return "Unknow UID: " + Long.toUnsignedString(uID);
    }
}
