package ChatAnwendung.Api;

import ChatAnwendung.Impl.RoutingEntryImpl;

import java.net.InetAddress;
import java.util.List;

public interface RoutingTable {

    void add(RoutingEntryImpl entry);

    boolean isUIDavailable(long uid);

    List<RoutingEntry> getAllEntries();

    InetAddress getNextHopAdressForUID(long uID);

    void removeUID(long uID);

    int getNextHopPortForUID(long uID);

    List<RoutingEntry> getAllDirectNeighbours();
}
