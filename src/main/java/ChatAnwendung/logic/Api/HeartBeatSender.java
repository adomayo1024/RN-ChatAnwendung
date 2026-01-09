package ChatAnwendung.logic.Api;

public interface HeartBeatSender {

    /**
     * Sendet an alle direkten Nachbarn einen HeartBeat. Um zu zeigen, dass man noch aktiv ist.
     * Wenn es keinen direkten Nachbarn gibt, wird nichts gesendet.
     */
    void sendHeartbeat();
}
