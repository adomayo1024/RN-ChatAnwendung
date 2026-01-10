package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.HeartBeatSender;
import ChatAnwendung.logic.Api.ScheduledTaskHandler;
import ChatAnwendung.logic.Api.TimeOutHandler;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.persistence.Api.Storage;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;

/**
 * Der ScheduledTaskHandler kümmert sich um alle Aufgaben, die in einem gewissen Zeitintervall immer wieder passieren.
 * - TimeoutHandling (alle 10 Sekunden)
 * - HeartbeatSending (alle 5 Sekunden)
 * - RoutingTableSending (alle 10 Sekunden)
 */
public class ScheduledTasksHandlerImpl implements ScheduledTaskHandler {

    // Der HeartbeatSender sendet alle 5 Sekunden Heartbeats an alle Nachbarn
    private final HeartBeatSender heartbeatSender;

    // Der RoutingTableSender sendet alle 10 Sekunden Heartbeats an alle Nachbarn
    private final RoutingTableSender routingTableSender;

    //Der TimeoutHandler prüft alle 10 Sekunden, alle Einträge nach Timeouts
    private final TimeOutHandler timeoutHandler;

    // Boolean, ob 10 Sekunden vergangen sind, seit dem letzten RoutingTableSending/TimeoutHandling
    private boolean tenSecoundsAgo;

    public ScheduledTasksHandlerImpl(RoutingTable routingTable, Storage storage, BlockingQueue<DatagramPacket> sendeQueue){
        heartbeatSender = new HeartbeatSenderImpl(routingTable, storage, sendeQueue);
        routingTableSender = new RoutingTableSender(routingTable, sendeQueue, storage);
        timeoutHandler = new TimeoutHandlerImpl(routingTable);
        tenSecoundsAgo = false;
    }

    @Override
    public void run(){

        // Prüft, ob RoutingTableSending/TimeoutHandling in diesen run dran sind.
        if(tenSecoundsAgo){
            routingTableSender.run();
            timeoutHandler.checkTimeouts();
        }

        heartbeatSender.sendHeartbeat();

        tenSecoundsAgo = !tenSecoundsAgo;
    }
}
