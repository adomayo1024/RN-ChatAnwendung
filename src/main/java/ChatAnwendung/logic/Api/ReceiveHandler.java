package ChatAnwendung.logic.Api;

/**
 * Der ReceiverHandler verarbeitet alle eingehenden Pakete.
 */
public interface ReceiveHandler extends Runnable {

    /**
     * Der Loop, wo alle eingehenden Pakete verarbeitet werden.
     */
    void run();
}
