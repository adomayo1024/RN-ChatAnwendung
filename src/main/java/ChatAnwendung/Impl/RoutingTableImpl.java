package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RoutingTableImpl implements RoutingTable {

    private  Map<Long, RoutingEntry> entries;

    private ReentrantLock mutex;

    private static RoutingTable INSTANCE;

    private RoutingTableImpl() {
        entries = new HashMap<>();
        mutex = new ReentrantLock(true);
        add(new RoutingEntryImpl(0, InetAddress.getLoopbackAddress(), 8080, 1, System.currentTimeMillis()));
    }

    public static RoutingTable getInstance(){
        if(INSTANCE == null) {
            INSTANCE = new RoutingTableImpl();
        }
        return INSTANCE;
    }

    @Override
    public void add(RoutingEntry entry) {
        mutex.lock();
        if(entries.containsKey(entry.getUID())) {
            if(newEntryIsBetter(entry)) {
                entries.put(entry.getUID(), entry);
            }
        }
        else {
            entries.put(entry.getUID(),entry);
        }
        mutex.unlock();
    }

    private boolean newEntryIsBetter(RoutingEntry entry) {
        // TODO richtig implementieren nach Protokoll
        return true;
    }

    @Override
    public boolean isUIDavailable(long uid) {
        return true;
    }

    @Override
    public List<RoutingEntry> getAllEntries(){

        List<RoutingEntry> result = new ArrayList<>();

        mutex.lock();
        for(long keys : entries.keySet()){
            result.add(entries.get(keys));
        }
        mutex.unlock();

        return result;
    }

    @Override
    public InetAddress getNextHopAdressForUID(long uID) {
        return entries.get(uID).getNextHopAdress();
    }

    @Override
    public void removeUID(long uID) {
        mutex.lock();
        entries.remove(uID);
        mutex.unlock();
    }

    @Override
    public void removeUIDThroughGoodbye(long uID){
        mutex.lock();
        if(entries.containsKey(uID)) {
            InetAddress adressFromUID = entries.get(uID).getNextHopAdress();
            int portFromUID = entries.get(uID).getNextHopPort();

            for(Long key: entries.keySet() ){
                RoutingEntry entry = entries.get(key);
                if(adressFromUID.equals(entry.getNextHopAdress()) && entry.getNextHopPort() == portFromUID){
                    entry.setNextHopPort(-1);
                    entry.setNextHopAdress(null);
                    entry.setHops(-1);
                }
            }

            removeUID(uID);
        }
        mutex.unlock();

    }

    @Override
    public int getNextHopPortForUID(long uID) {
        mutex.lock();
        int result = entries.get(uID).getNextHopPort();
        mutex.unlock();
        return result;
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

        return result;
    }
}
