package ChatAnwendung.Impl.Exceptions;

public class NotAUIDException extends RuntimeException {

    private String msg;

    public NotAUIDException(String message) {
        msg = message;
    }

    @Override
    public String getMessage() {
        return "Wrong Format for a UID: " + msg.split(" ")[3] + " Not a Long";
    }
}
