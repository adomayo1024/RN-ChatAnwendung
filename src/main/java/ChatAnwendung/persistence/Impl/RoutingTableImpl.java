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

    // Map NodeIds zu RoutingEntries
    private final Map<Long, RoutingEntry> entries;

    //Mutex für die entries Map
    private final ReentrantLock mutex;

    public RoutingTableImpl() {
        entries = new HashMap<>();
        mutex = new ReentrantLock(true);
    }

    @Override
    public void add(RoutingEntry entry) {
        mutex.lock();

        //Prüft, ob der Eintrag bereits vorhanden ist (wichtig fürs Hinzufügen)
        if(entries.containsKey(entry.getNodeId())) {
            //Prüft, ob der neue Eintrag "besser" ist als der alte
            if(newEntryIsBetter(entry)) {
                entries.put(entry.getNodeId(), entry);
            }
        }
        // Der Eintrag ist komplett neu und kann hinzugefügt werden.
        else {
            entries.put(entry.getNodeId(),entry);
            log.info("User: {} is available for Chatting", Long.toUnsignedString(entry.getNodeId()));
        }
        mutex.unlock();

        log.debug("Added new Entry to RoutingTable: {}", this);
    }

    /**
     * Prüft, ob ein neuer Eintrag "besser" ist als der alte. Um zu gucken, ob er den Alten ersetzen sollte.
     * Er ist besser, wenn:
     * a: Der Alte nicht erreichbar ist.
     * b: Der Alte hat eine höhere Hops Anzahl als der Neue.
     * c: Der Neue hat eine andere nextIp als der Alte oder eine andere nextPort als der Alte.
     * @param newEntry Der neue Eintrag, der überprüft werden soll.
     * @return True, wenn der neue Eintrag besser ist als der alte, sonst false.
     */
    private boolean newEntryIsBetter(RoutingEntry newEntry) {
        RoutingEntry oldEntry = entries.get(newEntry.getNodeId());
        boolean result;

        //Prüft, ob Alter überhaupt noch da ist
        if(oldEntry == null){
         return true;
        }
        //Prüft, ob der alte Eintrag nicht erreichbar ist
        else if(!oldEntry.getRoutable()){
            result = true;
        }
        //Prüft, ob der Alte eine höhere Hops Anzahl hat
        else if(oldEntry.getHops() > newEntry.getHops()) {
            result = true;
        }
        //Prüft, ob der neue Eintrag eine andere IP als der alte hat oder einen anderen Port als der alte hat.
        else {
            result = oldEntry.getNextHopAddress().equals(newEntry.getNextHopAddress()) &&
                    oldEntry.getNextHopPort() == newEntry.getNextHopPort();
        }
        return result;
    }

    @Override
    public boolean isNodeIdAvailable(long nodeId) {
        return entries.containsKey(nodeId) && entries.get(nodeId).getRoutable();
    }

    @Override
    public List<RoutingEntry> getAllEntries(){

        List<RoutingEntry> result = new ArrayList<>();

        mutex.lock();
        //Kopiert alle Einträge in die Result Liste
        for(long keys : entries.keySet()){
            result.add(entries.get(keys));
        }
        mutex.unlock();

        log.debug("Returned all Entries from RoutingTable: {}", this);

        return result;
    }

    @Override
    public InetAddress getNextHopAddressForUID(long nodeId) {
        if(entries.containsKey(nodeId)){
            return entries.get(nodeId).getNextHopAddress();
        }
        return null;
    }

    @Override
    public void removeUID(long NodeId) {
        mutex.lock();
        entries.remove(NodeId);

        log.debug("Removed UID: {} from RoutingTable: {}", Long.toUnsignedString(NodeId), this);

        mutex.unlock();
    }

    @Override
    public void removeUIDThroughGoodbye(long nodeId){

        //Prüft, ob es einen Eintrag für die nodeId gibt
        if(entries.containsKey(nodeId)) {
            InetAddress addressFromUID = entries.get(nodeId).getNextHopAddress();
            int portFromUID = entries.get(nodeId).getNextHopPort();

            mutex.lock();
            //Geht alle Einträge durch und guckt, ob der User der next Hop wäre für die Einträge
            for(Long key: entries.keySet() ){
                RoutingEntry entry = entries.get(key);
                if(addressFromUID.equals(entry.getNextHopAddress()) && entry.getNextHopPort() == portFromUID){
                    //Setzt den Eintrag auf nicht erreichbar
                    entry.setNextHopPort(-1);
                    entry.setNextHopAddress(null);
                    entry.setHops((byte)-1);
                    entry.setRoutable(false);

                    log.debug("Changed RoutingEntry to not routable: {}", Long.toUnsignedString(entry.getNodeId()));
                }
            }
            mutex.unlock();

            //Entfernt den Eintrag aus der Routing Tabelle
            removeUID(nodeId);

            log.info("User {} logged out", Long.toUnsignedString(nodeId));

            log.debug("Removed UID: {} from RoutingTable: {}", Long.toUnsignedString(nodeId), this);
        }

    }

    @Override
    public int getNextHopPortForUID(long nodeId) {
        if(entries.containsKey(nodeId)){
            return entries.get(nodeId).getNextHopPort();
        }
        return -1;
    }

    @Override
    public List<RoutingEntry> getAllDirectNeighbours() {

        List<RoutingEntry> result = new ArrayList<>();

        mutex.lock();
        //Kopiert alle direkten Nachbarn (hops == 1) in die result Liste
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
    public void setLastSeen(long nodeId){
        mutex.lock();
        if(entries.containsKey(nodeId)){
            entries.get(nodeId).setLastSeen();
        }
        mutex.unlock();

        log.debug("Set LastSeen for UID: {} in RoutingTable: {}", Long.toUnsignedString(nodeId), this);
    }

    @Override
    public void removeAll() {
        entries.clear();
        log.debug("Removed all Entries from RoutingTable: {}", this);
    }
}
