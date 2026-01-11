package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.TimeOutHandler;
import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TimeoutHandlerImpl implements TimeOutHandler {

    // Nach der Zeit in Millisekunden, nach der ein RoutingEntry als Timeout markiert wird und entfernt wird
    private final int timeout = 30_000;

    // Die Routing table, um den Eintrag wegen Timeouts zu entfernen
    private final RoutingTable routingTable;

    public TimeoutHandlerImpl(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public void checkTimeouts() {

        //Prüft alle RoutingEntries auf Timeout
        for(RoutingEntry entry : routingTable.getAllEntries()){
            int lastSeen = (int)(System.currentTimeMillis() - entry.getLastSeen());
            if(lastSeen >= timeout){
                routingTable.removeUID(entry.getNodeId());
                log.info("RoutingEntry removed for {} because of Timeout", Long.toUnsignedString(entry.getNodeId()));
            }
        }

    }
}
