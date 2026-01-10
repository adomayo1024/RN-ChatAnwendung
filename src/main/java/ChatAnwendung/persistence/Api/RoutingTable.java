package ChatAnwendung.persistence.Api;

import java.net.InetAddress;
import java.util.List;

/**
 * Eine RoutingTable speichert alle verfügbaren User und wie diese erreichbar sind.
 */
public interface RoutingTable {

    /**
     * Fügt einen neuen RoutingEntry hinzu.
     * @param entry Der Entry, der hinzugefügt werden soll
     */
    void add(RoutingEntry entry);

    /**
     * Prüft, ob es einen Eintrag gibt für die {@code nodeId}.
     * @param nodeId Die NodeId die überprüft werden soll.
     * @return True, wenn es einen Eintrag gibt, sonst false.
     */
    boolean isNodeIdAvailable(long nodeId);

    /**
     * Gibt alle Einträge der RoutingTable zurück
     * @return Eine Liste mit allen Einträgen der RoutingTable.
     */
    List<RoutingEntry> getAllEntries();

    /**
     * Gibt die nextHopAddress wieder, für die {@code NodeId}.
     * @param nodeId Die NodeId, für die die nextHopAddress zurückgegeben werden soll.
     * @return Die NextHopAddress
     */
    InetAddress getNextHopAddressForUID(long nodeId);

    /**
     * Entfernt den Eintrag, der diese {@code NodeId} hat, aus der RoutingTable.
     * @param NodeId Die NodeId von dem Eintrag der entfernt werden soll.
     */
    void removeUID(long NodeId);

    /**
     * Entfernt den Eintrag aus der RoutingTable mit der {@code NodeId}, zudem werden alle Einträge, die diese als nextHop
     * haben, werden auf nicht erreichbar gesetzt.
     * @param nodeId Die NodeId des Eintrags, der aus der RoutingTable entfernt werden soll.
     */
    void removeUIDThroughGoodbye(long nodeId);

    /**
     * Gibt den nextHopPort, für die {@code NodeId} wieder,
     * @param nodeId Die NodeId, für die der nextHopPort zurückgegeben werden soll.
     * @return Der nextHopPort für die {@code NodeId}
     */
    int getNextHopPortForUID(long nodeId);

    /**
     * Gibt alle direkten Nachbarn wieder. Direkte Nachbarn sind welche wo Hops gleich 1 sind.
     * @return List alle direkten Nachbarn.
     */
    List<RoutingEntry> getAllDirectNeighbours();

    /**
     * Setzt den Zeitpunkt, zu dem der User, mit der {@code NodeId} zuletzt gesehen wurde, auf den aktuellen Zeitpunkt.
     * @param nodeId Die NodeId des Users, der den Zeitpunkt aktualisiert werden soll.
     */
    void setLastSeen(long nodeId);

    /**
     * Entfernt alle Einträge aus der RoutingTable.
     */
    void removeAll();
}
