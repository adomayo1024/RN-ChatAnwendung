package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@Slf4j
public class TimeoutHandler extends AbstractHandler {

    private final int timeout = 45_000;

    private final RoutingTable routingTable;

    public TimeoutHandler(RoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    @Override
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
