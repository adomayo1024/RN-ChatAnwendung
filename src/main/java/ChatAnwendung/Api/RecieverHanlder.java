package ChatAnwendung.Api;

import java.net.DatagramPacket;

public interface RecieverHanlder {

    void handle(DatagramPacket packet);
}
