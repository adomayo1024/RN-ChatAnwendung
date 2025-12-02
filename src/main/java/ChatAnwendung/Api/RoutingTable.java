package ChatAnwendung.Api;

import ChatAnwendung.Impl.RoutingEntryImpl;

import java.net.InetAddress;
import java.util.List;

public interface RoutingTable {

    public void add(RoutingEntryImpl entry);

    boolean isUIDavailable();

    List<RoutingEntry> getAllEntries();

    InetAddress getNextHopAdressForUID(long uID);

    void removeUID(long uID);

    int getNextHopPortFroUID(long uID);
}
