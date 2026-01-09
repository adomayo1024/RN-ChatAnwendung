package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.HeartBeatSender;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.persistence.Api.Storage;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

public class loopServices implements Runnable{

    private HeartBeatSender heartbeatSender;

    private RoutingTableSender routingTableSender;

    private TimeoutHandler timeoutHandler;

    private boolean routingTableSending;

    public loopServices(RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue){
        heartbeatSender = new HeartbeatSenderImpl(routingTable, storage, sendeQueue);
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

        heartbeatSender.sendHeartbeat();

        routingTableSending = !routingTableSending;
    }
}
