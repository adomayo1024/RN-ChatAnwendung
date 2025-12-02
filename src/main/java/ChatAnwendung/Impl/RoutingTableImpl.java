package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;

import java.net.InetAddress;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RoutingTableImpl implements RoutingTable {

    private static Map<Long, RoutingEntryImpl> entries;

    public RoutingTableImpl() {
        entries = new HashMap<>();
        add(new RoutingEntryImpl(0, InetAddress.getLoopbackAddress(), 8080, 1, System.currentTimeMillis()));
    }

    @Override
    public void add(RoutingEntryImpl entry) {
        if(entries.containsKey(entry.getUID())) {
            if(newEntryIsBetter(entry)) {
                entries.put(entry.getUID(), entry);
            }
        }
        else {
            entries.put(entry.getUID(),entry);
        }
    }

    private boolean newEntryIsBetter(RoutingEntryImpl entry) {
        // TODO richtig implementieren nach Protokoll
        return true;
    }

    @Override
    public boolean isUIDavailable() {
        return true;
    }

    @Override
    public List<RoutingEntry> getAllEntries(){

        List<RoutingEntry> result = new ArrayList<>();

        for(long keys : entries.keySet()){
            result.add(entries.get(keys));
        }

        return result;
    }

    @Override
    public InetAddress getNextHopAdressForUID(long uID) {
        return entries.get(uID).getAdress();
    }

    @Override
    public void removeUID(long uID) {
        entries.remove(uID);
    }

    @Override
    public int getNextHopPortFroUID(long uID) {
        return entries.get(uID).getPort();
    }
}
