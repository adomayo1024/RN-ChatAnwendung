package ChatAnwendung.logic.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import ChatAnwendung.persistence.Api.RoutingTable;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class TimeoutHandler {

    private final int timeout = 30_000;

    private final RoutingTable routingTable;

    public TimeoutHandler(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    public void run() {
        for(RoutingEntry entry : routingTable.getAllEntries()){
            int lastSeen = (int)(System.currentTimeMillis() - entry.getLastSeen());
            if(lastSeen >= timeout){
                routingTable.removeUID(entry.getUID());
                log.debug("RoutingEntry removed for {} because of Timeout", Long.toUnsignedString(entry.getUID()));
                System.out.println("RoutingEntry removed for " + Long.toUnsignedString(entry.getUID()) + " because of Timeout");
            }
        }

    }
}
