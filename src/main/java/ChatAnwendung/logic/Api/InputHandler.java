package ChatAnwendung.logic.Api;

/**
 * Verarbeitet alle Inputs des Users.
 */
public interface InputHandler {
    /**
     * Der Loop, wo alle Inputs des Users verarbeitet werden.
     * @param stdIn Der Input des Users
     */
    void handle(String stdIn);
}
