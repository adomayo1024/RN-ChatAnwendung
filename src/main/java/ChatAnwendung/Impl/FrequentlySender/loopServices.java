package ChatAnwendung.Impl.FrequentlySender;

import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.TimeoutHandler;
import ChatAnwendung.Impl.persistence.Storage;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

public class loopServices implements Runnable{

    private HearbeatSender heartbeatSender;

    private RoutingTableSender routingTableSender;

    private TimeoutHandler timeoutHandler;

    private boolean routingTableSending;

    public loopServices(RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue){
        heartbeatSender = new HearbeatSender(routingTable, storage, sendeQueue);
        routingTableSender = new RoutingTableSender(routingTable, sendeQueue, storage);
        timeoutHandler = new TimeoutHandler(routingTable);
        routingTableSending = false;
    }

    @Override
    public void run(){
        if(routingTableSending){
            routingTableSender.run();
            timeoutHandler.run();
        }

        heartbeatSender.run();

        routingTableSending = !routingTableSending;
    }
}
