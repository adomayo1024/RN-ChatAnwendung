package ChatAnwendung.logic.Api;

/**
 * Der ScheduledTaskHandler kümmert sich um alle Aufgaben, die in einem gewissen Zeitintervall immer wieder passieren.
 * - TimeoutHandling (alle 10 Sekunden)
 * - HeartbeatSending (alle 5 Sekunden)
 * - RoutingTableSending (alle 10 Sekunden)
 */
public interface ScheduledTaskHandler extends Runnable{
}
