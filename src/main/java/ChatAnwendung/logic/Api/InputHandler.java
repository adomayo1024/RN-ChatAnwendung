package ChatAnwendung.logic.Api;

/**
 * Verarbeitet alle Inputs des Users.
 */
public interface InputHandler extends Runnable{
    /**
     * Der Loop, wo alle Inputs des Users verarbeitet werden.
     */
    void run();
}
