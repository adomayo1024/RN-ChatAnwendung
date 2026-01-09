package ChatAnwendung.logic.Api;

public interface TimeOutHandler {

    /**
     * Prüft alle Routing Einträge auf Timeouts.
     * Wenn einer sich seit Timeout Zeit nicht gemeldet hat, wird er aus der Routing Tabelle entfernt.
     */
    void checkTimeouts();
}
