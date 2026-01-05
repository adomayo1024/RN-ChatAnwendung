package ChatAnwendung.persistence.Api;

import java.net.InetAddress;

public interface RoutingEntry {
    Long getUID();

    int getNextHopPort();

    void setNextHopPort(int port);

    InetAddress getNextHopAdress();

    void setNextHopAdress(InetAddress adress);

    byte getHops();

    void setHops(byte hops);

    boolean isRoutable();

    void setRoutable(boolean routable);

    long getLastSeen();

    long getLastSeenShort();

    void setLastSeen();
}
