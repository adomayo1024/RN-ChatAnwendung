package ChatAnwendung.logic.Api;

import ChatAnwendung.logic.Enums.PacketTypes;

import java.net.DatagramPacket;
import java.net.InetAddress;

public interface BCPPacket {

    //------ BCP-Header Variablen ------


    // Größe des BCP-Headers
      int HEADER_SIZE = 38;

    //Größe des Versionsfeldes im BCP-Header in Bytes
      int VERSION_SIZE = 1;

    //Größe des Typs Feldes im BCP-Header in Bytes
      int TYPE_SIZE = 1;

    //Größe des TTL Feldes im BCP-Header in Bytes
      int TTL_SIZE = 1;

    //Größe des Hops Feldes im BCP-Header in Bytes
      int HOPS_SIZE = 1;

    //Größe des SrcNode Feldes im BCP-Header in Bytes
      int SRC_NODE_SIZE = 8;

    //Größe des DesNode Feldes im BCP-Header in Bytes
      int DEST_NODE_SIZE = 8;

    //Größe des Sequenzfeldes im BCP-Header in Bytes
      int SEQUENZ_SIZE = 4;

    //Größe des FileId Feldes im BCP-Header in Bytes
      int FILE_ID_SIZE = 4;

    //Größe des CRC Feldes im BCP-Header in Bytes
      int CRC_SIZE = 8;

    //Größe des PayloadLength Feldes im BCP-Header in Bytes
      int PAYLOAD_LENGTH_SIZE = 2;

    //Position des Versionsfeldes im BCP-Header
      int VERSION_POS = 0;

    //Position des Typs Feldes im BCP-Header
      int TYPE_POS = VERSION_POS + VERSION_SIZE;

    //Position des TTL Feldes im BCP-Header
      int TTL_POS = TYPE_POS + TYPE_SIZE;

    //Position des Hops Feldes im BCP-Header
      int HOPS_POS = TTL_POS + TTL_SIZE;

    //Position des SrcNode Feldes im BCP-Header
      int SRC_NODE_ID_POS = HOPS_POS + HOPS_SIZE;

    //Position des DestNode Feldes im BCP-Header
      int DEST_NODE_ID_POS = SRC_NODE_ID_POS + SRC_NODE_SIZE;

    //Position des Sequenz Feldes im BCP-Header
      int SEQUENZ_POS = DEST_NODE_ID_POS + DEST_NODE_SIZE;

    //Position des FileId Feldes im BCP-Header
      int FILE_ID_POS = SEQUENZ_POS + SEQUENZ_SIZE;

    //Position des CRC Feldes im BCP-Header
      int CRC_POS = FILE_ID_POS + FILE_ID_SIZE;

    //Position des PayloadLength Feldes im BCP-Header
      int PAYLOAD_LENGTH_POS = CRC_POS + CRC_SIZE;

    //Position des Payloads
      int PAYLOAD_POS = PAYLOAD_LENGTH_POS + PAYLOAD_LENGTH_SIZE;

    // Maximale Größe des Payloads in Bytes
      int MAXIMUM_PAYLOAD_SIZE = 1300;


    //------ BCP-FILE_INIT Payload Variablen ------

    //Größe des Dateinamens im BCP-FILE_INIT-Payload in Bytes
     int FILE_INIT_FILE_SIZE_SIZE = 4;

    // Position der Dateigröße im BCP-FILE_INIT-Payload
      int FILE_INIT_FILE_SIZE_POS = 0;

    //Position des Dateinamens im BCP-FILE_INIT-Payload
      int ROUTING_TABLE_FILE_NAME_POS = FILE_INIT_FILE_SIZE_POS + FILE_INIT_FILE_SIZE_SIZE;

    //------ BCP-ROUTING-TABLE Payload Variablen ------

    
      int ROUTING_TABLE_DEST_NODE_ID_SIZE = 8;

    
      int ROUTING_TABLE_HOPS_SIZE = 1;

    
      int ROUTING_TABLE_LAST_SEEN_SIZE = 8;

    
      int ROUTING_TABLE_ENTRY_SIZE = ROUTING_TABLE_DEST_NODE_ID_SIZE + ROUTING_TABLE_HOPS_SIZE + ROUTING_TABLE_LAST_SEEN_SIZE;

    
      int ROUTING_TABLE_DEST_NODE_ID_POS = 0;

    
      int ROUTING_TABLE_HOPS_POS = ROUTING_TABLE_DEST_NODE_ID_POS + ROUTING_TABLE_DEST_NODE_ID_SIZE;

    
      int ROUTING_TABLE_LAST_SEEN_POS = ROUTING_TABLE_HOPS_POS + ROUTING_TABLE_HOPS_SIZE;
    
    
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
     * Die Größe der Datei, die heruntergeladen werden soll.
     * Geht aber nur wenn der Typ der Datei "FILE_INIT" ist.
     * @return Größe der Datei in Bytes oder -1, wenn der Typ nicht "FILE_INIT" ist.
     */
    int getFileSize();

    /**
     * Gibt die NodeId des Routingtabellen Eintrags wieder, der mit diesem BCP-Packet verschickt worden ist.
     * Geht aber nur, wenn der Typ der Datei "ROUTING-TABLE" ist.
     * @param offset Gibt an, wo der Anfang der Node Id im Payload liegt.
     *               (RoutingTableSendSize * der wievielte Eintrag im Payload)
     * @return Die NodeId des Routingtabellen Eintrags, oder -1, wenn der Typ nicht "ROUTING-TABLE" ist.
     */
    long getNodeIdFromRoutingTableEntry(int offset);

    /**
     * Gibt den Wert von LastSeen des Routingtabellen Eintrags wieder, der mit diesem BCP-Packet verschickt worden ist.
     * Geht aber nur, wenn der Typ der Datei "ROUTING-TABLE" ist.
     * @param offset Gibt an, wo der Anfang des Wertes von LastSenn im Payload liegt.
     *               (RoutingTableSendSize * der wievielte Eintrag im Payload)
     * @return Der Wert von LastSeen des Routingtabellen Eintrags, oder -1, wenn der Typ nicht "ROUTING-TABLE" ist.
     */
    long getLastSeenFromRoutingTableEntry(int offset);

    /**
     * Gibt den Wert von Hops des Routingtabellen Eintrags wieder, der mit diesem BCP-Packet verschickt worden ist.
     * Geht aber nur, wenn der Typ der Datei "ROUTING-TABLE" ist.
     * @param offset Gibt an, wo der Anfang des Wertes von Hops im Payload liegt.
     *               (RoutingTableSendSize * der wievielte Eintrag im Payload)
     * @return Der Wert von Hops des Routingtabellen Eintrags, oder -1, wenn der Typ nicht "ROUTING-TABLE" ist.
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


    //--------- GETTER ---------

    PacketTypes getType();
    
    byte getTtl();
    
    byte getHops();
    
    long getSrcNodeId();
    
    long getDestNodeId();
    
    int getSequenz();
    
    int getFileId();
    
    long getCrc();
    
    short getPayloadLength();
    
    byte[] getPayload();
    
    InetAddress getAddress();
    
    int getPort();
    
    
    //--------- SETTER ---------
    
    void setType(PacketTypes type);
    
    void setTtl(byte ttl);
    
    void setHops(byte hops);
    
    void setSrcNodeId(long srcNodeId);
    
    void setDestNodeId(long destNodeId);
    
    void setSequenz(int sequenz);
    
    void setFileId(int fileId);
    
    void setCrc(long crc);
    
    void setPayloadLength(short payloadLength);
    
    void setPayload(byte[] payload);
    
    void setAddress(InetAddress address);
    
    void setPort(int port);
}
