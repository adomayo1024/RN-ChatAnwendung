package ChatAnwendung.persistence.Api;

import java.net.InetAddress;

public interface RoutingEntry {

    long getNodeId();

    int getNextHopPort();

    void setNextHopPort(int port);

    InetAddress getNextHopAddress();

    void setNextHopAddress(InetAddress address);

    byte getHops();

    void setHops(byte hops);

    boolean getRoutable();

    void setRoutable(boolean routable);

    long getLastSeen();

    long getLastSeenShort();

    void setLastSeen();
}
