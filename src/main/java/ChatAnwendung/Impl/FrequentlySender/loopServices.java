package ChatAnwendung.Impl.FrequentlySender;

import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.TimeoutHandler;

public class loopServices extends AbstractHandler {

    private HearbeatSender heartbeatSender;

    private RoutingTableSender routingTableSender;

    private TimeoutHandler timeoutHandler;

    private boolean routingTableSending;

    public loopServices(RoutingTable routingTable){
        heartbeatSender = new HearbeatSender();
        routingTableSender = new RoutingTableSender();
        timeoutHandler = new TimeoutHandler(routingTable);
        routingTableSending = false;
    }

    @Override
    public void run(){
        if(routingTableSending){
            routingTableSender.run();
        }

        heartbeatSender.run();

        routingTableSending = !routingTableSending;
    }
}
