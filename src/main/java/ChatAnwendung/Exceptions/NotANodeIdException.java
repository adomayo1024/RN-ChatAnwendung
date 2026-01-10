package ChatAnwendung.Exceptions;

public class NotANodeIdException extends RuntimeException {

    private final String msg;

    public NotANodeIdException(String message) {
        msg = message;
    }

    @Override
    public String getMessage() {
        return "Wrong Format for a UID: " + msg.split(" ")[3] + " Not a Long";
    }
}
