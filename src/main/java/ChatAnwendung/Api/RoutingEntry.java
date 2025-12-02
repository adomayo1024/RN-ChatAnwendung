package ChatAnwendung.Api;

import java.net.InetAddress;

public interface RoutingEntry {
    Long getUID();

    int getPort();

    InetAddress getAdress();

    int getHops();

    long getLast_seen();
}
