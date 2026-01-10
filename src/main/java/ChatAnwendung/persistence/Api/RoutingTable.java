package ChatAnwendung.persistence.Api;

import java.net.InetAddress;
import java.util.List;

public interface RoutingTable {

    void add(RoutingEntry entry);

    boolean isNodeIdAvailable(long uid);

    List<RoutingEntry> getAllEntries();

    InetAddress getNextHopAddressForUID(long uID);

    void removeUID(long uID);

    void removeUIDThroughGoodbye(long uID);

    int getNextHopPortForUID(long uID);

    List<RoutingEntry> getAllDirectNeighbours();

    void setLastSeen(long uID);

    void removeAll();
}
