package ChatAnwendung.Api;

import java.net.InetAddress;

public interface RoutingEntry {
    Long getUID();

    int getNextHopPort();

    void setNextHopPort(int port);

    InetAddress getNextHopAdress();

    void setNextHopAdress(InetAddress adress);

    int getHops();

    void setHops(int hops);

    boolean isRoutable();

    void setRoutable(boolean routable);

    long getLast_seen();
}
