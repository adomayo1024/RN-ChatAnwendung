package ChatAnwendung.persistence.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RoutingTableImpl implements RoutingTable {

    // TODO ändern wenn andere 18 verwenden

    private static final int routingEntrySize = 17;

    private final Map<Long, RoutingEntry> entries;

    private final ReentrantLock mutex;

    public RoutingTableImpl() {
        entries = new HashMap<>();
        mutex = new ReentrantLock(true);
    }

    @Override
    public void add(RoutingEntry entry) {
        mutex.lock();
        if(entries.containsKey(entry.getNodeId())) {
            if(newEntryIsBetter(entry)) {
                entries.put(entry.getNodeId(), entry);
            }
        }
        else {
            entries.put(entry.getNodeId(),entry);
            System.out.println("User: " + Long.toUnsignedString(entry.getNodeId()) + " is available for Chatting");
        }
        mutex.unlock();

        log.debug("Added new Entry to RoutingTable: {}", this);
    }

    private boolean newEntryIsBetter(RoutingEntry newEntry) {
        RoutingEntry oldEntry = entries.get(newEntry.getNodeId());
        boolean result;

        if(oldEntry == null){
         return true;
        }
        else if(!oldEntry.getRoutable()){
            result = true;
        } else if(oldEntry.getHops() > newEntry.getHops()) {
            result = true;
        } else {
            result = oldEntry.getNextHopAddress().equals(newEntry.getNextHopAddress()) &&
                    oldEntry.getNextHopPort() == newEntry.getNextHopPort();
        }
        return result;
    }

    @Override
    public boolean isNodeIdAvailable(long uid) {

        return entries.containsKey(uid) && entries.get(uid).getRoutable();
    }

    @Override
    public List<RoutingEntry> getAllEntries(){

        List<RoutingEntry> result = new ArrayList<>();

        mutex.lock();
        for(long keys : entries.keySet()){
            result.add(entries.get(keys));
        }
        mutex.unlock();

        log.debug("Returned all Entries from RoutingTable: {}", this);

        return result;
    }

    @Override
    public InetAddress getNextHopAddressForUID(long uID) {
        if(entries.containsKey(uID)){
            return entries.get(uID).getNextHopAddress();
        }
        return null;
    }

    @Override
    public void removeUID(long uID) {
        mutex.lock();
        entries.remove(uID);

        log.debug("Removed UID: {} from RoutingTable: {}", Long.toUnsignedString(uID), this);

        mutex.unlock();
    }

    @Override
    public void removeUIDThroughGoodbye(long uID){

        if(entries.containsKey(uID)) {
            InetAddress addressFromUID = entries.get(uID).getNextHopAddress();
            int portFromUID = entries.get(uID).getNextHopPort();
            mutex.lock();
            for(Long key: entries.keySet() ){
                RoutingEntry entry = entries.get(key);
                if(addressFromUID.equals(entry.getNextHopAddress()) && entry.getNextHopPort() == portFromUID){
                    entry.setNextHopPort(-1);
                    entry.setNextHopAddress(null);
                    entry.setHops((byte)-1);
                    entry.setRoutable(false);

                    log.debug("Changed RoutingEntry to not routable: {}", Long.toUnsignedString(entry.getNodeId()));
                }
            }
            mutex.unlock();
            removeUID(uID);

            log.info("User {} logged out", Long.toUnsignedString(uID));
            System.out.println("User " + Long.toUnsignedString(uID) + " logged out");

            log.debug("Removed UID: {} from RoutingTable: {}", Long.toUnsignedString(uID), this);
        }

    }

    @Override
    public int getNextHopPortForUID(long uID) {
        if(entries.containsKey(uID)){
            return entries.get(uID).getNextHopPort();
        }
        return -1;
    }

    @Override
    public List<RoutingEntry> getAllDirectNeighbours() {

        List<RoutingEntry> result = new ArrayList<>();

        mutex.lock();
        for(long key: entries.keySet()){
            RoutingEntry entry = entries.get(key);
            if(entry.getHops() == 1) {
                result.add(entry);
            }
        }
        mutex.unlock();

        log.debug("Returned all direct Neighbours from RoutingTable: {}", this);

        return result;
    }

    @Override
    public void setLastSeen(long uID){
        mutex.lock();
        if(entries.containsKey(uID)){
            entries.get(uID).setLastSeen();
        }
        mutex.unlock();

        log.debug("Set LastSeen for UID: {} in RoutingTable: {}", Long.toUnsignedString(uID), this);
    }

    @Override
    public void removeAll() {
        entries.clear();
        log.debug("Removed all Entries from RoutingTable: {}", this);
    }
}
