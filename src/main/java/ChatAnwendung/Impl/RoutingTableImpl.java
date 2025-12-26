package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RoutingTableImpl implements RoutingTable {

    private  Map<Long, RoutingEntry> entries;

    private static final ReentrantLock getMutex = new ReentrantLock(true);

    private ReentrantLock mutex;

    private static RoutingTable INSTANCE;

    private RoutingTableImpl() {
        entries = new HashMap<>();
        mutex = new ReentrantLock(true);
    }

    public static RoutingTable getInstance(){
        getMutex.lock();
        if(INSTANCE == null) {
            INSTANCE = new RoutingTableImpl();
        }
        getMutex.unlock();
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

    private boolean newEntryIsBetter(RoutingEntry newEntry) {
        RoutingEntry oldEntry = entries.get(newEntry.getUID());
        boolean result;

        if(!oldEntry.isRoutable()){
            result = true;
        } else if(oldEntry.getHops() > newEntry.getHops()) {
            result = true;
        } else {
            result = oldEntry.getNextHopAdress().equals(newEntry.getNextHopAdress()) &&
                    oldEntry.getNextHopPort() == newEntry.getNextHopPort();
        }
        return result;
    }

    @Override
    public boolean isUIDavailable(long uid) {
        boolean result = false;

        if(entries.containsKey(uid) && entries.get(uid).isRoutable()){
            result = true;
        }
        return result;
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
        if(entries.containsKey(uID)){
            entries.remove(uID);

        }
        mutex.unlock();
    }

    @Override
    public void removeUIDThroughGoodbye(long uID){

        if(entries.containsKey(uID)) {
            InetAddress adressFromUID = entries.get(uID).getNextHopAdress();
            int portFromUID = entries.get(uID).getNextHopPort();
            mutex.lock();
            for(Long key: entries.keySet() ){
                RoutingEntry entry = entries.get(key);
                if(adressFromUID.equals(entry.getNextHopAdress()) && entry.getNextHopPort() == portFromUID){
                    entry.setNextHopPort(-1);
                    entry.setNextHopAdress(null);
                    entry.setHops(-1);
                    entry.setRoutable(false);
                }
            }
            mutex.unlock();
            removeUID(uID);
        }

    }

    @Override
    public int getNextHopPortForUID(long uID) {
        return entries.get(uID).getNextHopPort();
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

    @Override
    public void setLastSeen(long uID){
        mutex.lock();
        if(entries.containsKey(uID)){
            entries.get(uID).setLastSeen();
        }
        mutex.unlock();
    }

    @Override
    public void removeAllExceptHops1() {
        for(Long key : entries.keySet()){
            removeUID(key);
        }
    }
}
