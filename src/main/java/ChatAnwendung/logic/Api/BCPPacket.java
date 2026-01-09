package ChatAnwendung.logic.Api;

import java.net.DatagramPacket;

public interface BCPPacket {

    /**
     * Erstellt ein DatagramPacket aus dem BCPPacket, welches dann versendet werden kann.
     * @return Das DatagramPacket
     */
    DatagramPacket makeDatagramPacket();

    /**
     * Gibt den Namen der Datei wieder, die gesendet, mit diesem BCPPacket wurde.
     * Geht aber nur wenn der Typ der Datei "FILE_INIT" ist.
     * @return Der Dateiname, oder null, wenn der Typ nicht "FILE_INIT" ist.
     */
    String getFileName();

    /**
     * Die Größe der Datei die heruntergeladen werden soll.
     * Geht aber nur wenn der Typ der Datei "FILE_INIT" ist.
     * @return Größe der Datei in Bytes oder -1 wenn der Typ nicht "FILE_INIT" ist.
     */
    int getFileSize();

    /**
     * Gibt die NodeId des Routingtabellen Eintrags wieder, der mit diesen BCP-Packet verschickt worden ist.
     * Geht aber nur wenn der Typ der Datei "ROUTINGTABLE" ist.
     * @param offset Gibt an, wo der Anfang der Node Id im Payload liegt.
     *               (RoutingTableSendSize * der wievielte Eintrag im Payload)
     * @return Die NodeId des Routingtabellen Eintrags, oder -1, wenn der Typ nicht "ROUTINGTABLE" ist.
     */
    long getNodeIdFromRoutingTableEntry(int offset);

    /**
     * Gibt den Wert von LastSeen des Routingtabellen Eintrags wieder, der mit diesem BCP-Packet verschickt worden ist.
     * Geht aber nur wenn der Typ der Datei "ROUTINGTABLE" ist.
     * @param offset Gibt an, wo der Anfang des Wertes von LastSenn im Payload liegt.
     *               (RoutingTableSendSize * der wievielte Eintrag im Payload)
     * @return Der Wert von LastSeen des Routingtabellen Eintrags, oder -1, wenn der Typ nicht "ROUTINGTABLE" ist.
     */
    long getLastSeenFromRoutingTableEntry(int offset);

    /**
     * Gibt den Wert von Hops des Routingtabellen Eintrags wieder, der mit diesem BCP-Packet verschickt worden ist.
     * Geht aber nur wenn der Typ der Datei "ROUTINGTABLE" ist.
     * @param offset Gibt an, wo der Anfang des Wertes von Hops im Payload liegt.
     *               (RoutingTableSendSize * der wievielte Eintrag im Payload)
     * @return Der Wert von Hops des Routingtabellen Eintrags, oder -1, wenn der Typ nicht "ROUTINGTABLE" ist.
     */
    byte getHopsFromRoutingTableEntry(int offset);

    /**
     * Dekrementiert die TTL um 1 des BCP-Packets.
     */
    void dekrementTtl();

    /**
     * Inkrementiert den Hops des BCP-Packets um 1.
     */
    void inkrementHops();

    /**
     * Prüft, ob das BCP-Packet für den User mit der Node Id gedacht ist.
     * @param myId Die Node Id des Users, die überprüft werden soll, ob das Packet für ihn gedacht ist.
     * @return true wenn es für die Node Id gedacht ist, sonst false.
     */
    boolean isItForMe(long myId);

    /**
     * Berechnet den CRC des BCP-Packets. Mit den ECMA-182 Algorithmus. Es nimmt dafür den kompletten Header und den Payload des BCP-Packets ein.
     * Mit ausnahm des CRC-Feldes, welches mit 0 aufgefüllt wird, für die Berechnung.
     * @return den CRC des BCP-Packets.
     */
    long calculateCrc();
}
