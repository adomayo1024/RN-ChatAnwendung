package ChatAnwendung.Impl.FrequentlySender;

import ChatAnwendung.Impl.Handler.Common.AbstractHandler;

public class HeartbeatAndRoutingTableSchedule extends AbstractHandler {

    private HearbeatSender heartbeatSender;

    private RoutingTableSender routingTableSender;

    private boolean routingTableSending;

    public HeartbeatAndRoutingTableSchedule(){
        heartbeatSender = new HearbeatSender();
        routingTableSender = new RoutingTableSender();
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
