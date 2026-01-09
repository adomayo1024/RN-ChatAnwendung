package ChatAnwendung.persistence.Api;

import ChatAnwendung.persistence.Impl.Connection;

import java.util.List;

/**
 * Die ConnectionList speichert alle bisherigen Connections, die dieser Host hat.
 */
public interface ConnectionList {

    /**
     * Fügt eine Connection hinzu, mit dem der Host direkt Verbunden ist.
     * @param connection die Connection
     */
    void add(Connection connection);

    /**
     * Entfernt eine Connection aus, dieser Host ist nicht mehr direkt mit dieser Connection verbunden.
     * @param connection die connection die entfernt werden soll
     */
    void remove(Connection connection);

    /**
     * Gibt all Connection wieder, die zurzeit bestehen.
     * @return liste aller Connections.
     */
    List<Connection> getAllConnections();
}
