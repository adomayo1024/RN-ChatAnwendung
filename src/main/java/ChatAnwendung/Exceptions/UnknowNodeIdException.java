package ChatAnwendung.Exceptions;

public class UnknowNodeIdException extends Throwable {


    private final Long uID;

    public UnknowNodeIdException(Long uID) {
        this.uID = uID;
    }

    @Override
    public String getMessage() {
        return "Unknow UID: " + Long.toUnsignedString(uID);
    }
}
