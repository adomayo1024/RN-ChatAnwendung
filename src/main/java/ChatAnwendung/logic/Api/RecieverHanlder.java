package ChatAnwendung.logic.Api;

import java.net.DatagramPacket;

public interface RecieverHanlder extends Runnable {

    void handle(DatagramPacket packet);
}
