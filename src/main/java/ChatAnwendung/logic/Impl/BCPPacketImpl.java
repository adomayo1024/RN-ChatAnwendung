package ChatAnwendung.logic.Impl;

import ChatAnwendung.logic.Api.BCPPacket;
import ChatAnwendung.logic.Enums.PacketTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Setter
@Slf4j
@AllArgsConstructor
public class BCPPacketImpl implements BCPPacket {

    // CRC Polynom
    private static final long POLY = 0x42F0E1EBA9EA3693L;

    //CRC Lookup Table
    private static final long[] TABLE = new long[256];

    // Erstellung der Lookup Table für die Berechnung des CRC
    static {
        for (int i = 0; i < 256; i++) {
            long c = (long) i << 56;
            long crc = 0;
            for (int j = 0; j < 8; j++) {
                if (((crc ^ c) & 0x8000000000000000L) != 0) {
                    crc = (crc << 1) ^ POLY;
                } else {
                    crc <<= 1;
                }
                c <<= 1;
            }
            TABLE[i] = crc;
        }
    }

    //------ BCP-Header Variablen ------

    // Größe des BCP-Headers
    @Getter
    private static final int headerSize = 38;

    //Größe des Versionsfeldes im BCP-Header in Bytes
    @Getter
    private static final int versionSize = 1;

    //Größe des Typs Feldes im BCP-Header in Bytes
    @Getter
    private static final int typeSize = 1;

    //Größe des TTL Feldes im BCP-Header in Bytes
    @Getter
    private static final int ttlSize = 1;

    //Größe des Hops Feldes im BCP-Header in Bytes
    @Getter
    private static final int hopsSize = 1;

    //Größe des SrcNode Feldes im BCP-Header in Bytes
    @Getter
    private static final int srcNodeSize = 8;

    //Größe des DesNode Feldes im BCP-Header in Bytes
    @Getter
    private static final int destNodeSize = 8;

    //Größe des Sequenzfeldes im BCP-Header in Bytes
    @Getter
    private static final int sequenzSize = 4;

    //Größe des FileId Feldes im BCP-Header in Bytes
    @Getter
    private static final int fileIdSize = 4;

    //Größe des CRC Feldes im BCP-Header in Bytes
    @Getter
    private static final int crcSize = 8;

    //Größe des PayloadLength Feldes im BCP-Header in Bytes
    @Getter
    private static final int payloadLengthSize = 2;

    //Position des Versionsfeldes im BCP-Header
    @Getter
    private static final int versionPos = 0;

    //Position des Typ Feldes im BCP-Header
    @Getter
    private static final int typePos = versionPos + versionSize;

    //Position des TTL Feldes im BCP-Header
    @Getter
    private static final int ttlPos = typePos + typeSize;

    //Position des Hops Feldes im BCP-Header
    @Getter
    private static final int hopsPos = ttlPos + ttlSize;

    //Position des SrcNode Feldes im BCP-Header
    @Getter
    private static final int srcNodeIdPos = hopsPos + hopsSize;

    //Position des DestNode Feldes im BCP-Header
    @Getter
    private static final int destNodeIdPos = srcNodeIdPos + srcNodeSize;

    //Position des Sequenz Feldes im BCP-Header
    @Getter
    private static final int sequenzPos = destNodeIdPos + destNodeSize;

    //Position des FileId Feldes im BCP-Header
    @Getter
    private static final int fileIdPos = sequenzPos + sequenzSize;

    //Position des CRC Feldes im BCP-Header
    @Getter
    private static final int crcPos = fileIdPos + fileIdSize;

    //Position des PayloadLength Feldes im BCP-Header
    @Getter
    private static final int payloadLengthPos = crcPos + crcSize;

    //Position des Payloads
    @Getter
    private static final int payloadPos = payloadLengthPos + payloadLengthSize;

    // Maximale Größe des Payloads in Bytes
    @Getter
    private static final int maximumPayloadSize = 1300;


    //------ BCP-FILE_INIT Payload Variablen ------

    //Größe des Dateinamens im BCP-FILE_INIT-Payload in Bytes
    @Getter
    private static final int FileInitFileSizeSize = 4;

    // Position der Dateigröße im BCP-FILE_INIT-Payload
    @Getter
    private static final int FileInitFileSizePos = 0;

    //Position des Dateinamens im BCP-FILE_INIT-Payload
    @Getter
    private static final int routingTableFileNamePos = FileInitFileSizePos + FileInitFileSizeSize;

    //------ BCP-ROUTINGTABLE Payload Variablen ------

    @Getter
    private static final int routingTableDestNodeIdSize = 8;

    @Getter
    private static final int routingTableHopsSize = 1;

    @Getter
    private static final int routingTableLastSeenSize = 8;

    @Getter
    private static final int routingTableEntrySize = routingTableDestNodeIdSize+ routingTableHopsSize + routingTableLastSeenSize;

    @Getter
    private static final int routingTableDestNodeIdPos = 0;

    @Getter
    private static final int routingTableHopsPos = routingTableDestNodeIdPos + routingTableDestNodeIdSize;

    @Getter
    private static final int routingTableLastSeenPos = routingTableHopsPos + routingTableHopsSize;






    // Version
    @Getter
    private byte version;

    // Typ des Packets
    @Getter
    private PacketTypes type;

    // TTL des Packets
    @Getter
    private byte ttl;

    // Anzahl der zurückgelegten Hops von dem Packet
    @Getter
    private byte hops;

    // Sender NodeId
    @Getter
    private long srcNodeId;

    // Ziel NodeId
    @Getter
    private long destNodeId;

    // Sequenznummer des Packets
    @Getter
    private int sequenz;

    // FileId des Packets, wenn ein File gesendet wird, sonst 0
    @Getter
    private int fileId;

    // CRC Wert des Packets
    @Getter
    private long crc;

    // Payload Größe des Packets
    @Getter
    private short payloadLength;

    // Payload des Packets
    @Getter
    private byte[] payload;

    // Adresse des Nodes, der das Packet erhalten soll
    @Getter
    private InetAddress address;

    // Port des Nodes, der das Packet erhalten soll
    @Getter
    private int port;

    public BCPPacketImpl(DatagramPacket packet){
        version = getVersion(packet);
        type = getType(packet);
        ttl = getTtl(packet);
        hops = getHops(packet);
        srcNodeId = getSrcNodeId(packet);
        destNodeId = getDestNodeId(packet);
        sequenz = getSequenz(packet);
        fileId = getFileId(packet);
        crc = getCrc(packet);
        payloadLength = getPayloadLength(packet);
        payload = getPayload(packet);
        address = packet.getAddress();
        port = packet.getPort();

    }

    public DatagramPacket makeDatagramPacket(){
        byte[] packet = makeBCPPacketIntoBytes();
        DatagramPacket dP = new DatagramPacket(packet, packet.length);
        dP.setAddress(address);
        dP.setPort(port);
        return dP;

    }

    public String getFileName(){

        if(type != PacketTypes.FILE_INIT){
            return null;
        }

        byte[] name = new byte[payloadLength - FileInitFileSizeSize];
        System.arraycopy(payload, routingTableFileNamePos, name, 0, name.length);

        return new String(name, StandardCharsets.UTF_8);
    }

    public int getFileSize(){
        if(type != PacketTypes.FILE_INIT){
            return -1;
        }

        return ByteBuffer.wrap(payload, FileInitFileSizePos, FileInitFileSizeSize).getInt();
    }

    public long getNodeIdFromRoutingTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }

        return ByteBuffer.wrap(payload, offset, routingTableDestNodeIdSize).getLong();

    }

    public long getLastSeenFromRoutingTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }

        return ByteBuffer.wrap(payload, offset + routingTableLastSeenPos, routingTableLastSeenSize).getLong();
    }

    public byte getHopsFromRoutingTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }
        return payload[offset + destNodeSize];
    }

    public void dekrementTtl() {
        ttl--;
    }

    public void inkrementHops() {
        hops++;
    }

    public boolean isItForMe(long myId) {
        return destNodeId == myId || destNodeId == -1;
    }

    public long calculateCrc() {

        long tempCrc = crc;

        crc = 0L;

        byte[] data = makeBCPPacketIntoBytes();

        long calculateCrc = ByteBuffer.wrap(data, crcPos, crcSize).getLong();

        crc = tempCrc;

        return calculateCrc;
    }

    /**
     * Erstellt ein Byte-Array aus dem BCP-Packet.
     * @return Das Byte-Array, welches das BCP-Packet darstellt.
     */
    private byte[] makeBCPPacketIntoBytes(){

        ByteBuffer buffer = ByteBuffer.allocate(payloadLength + headerSize);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put(version);
        buffer.put((byte)type.ordinal());
        buffer.put(ttl);
        buffer.put(hops);
        buffer.putLong(srcNodeId);
        buffer.putLong(destNodeId);
        buffer.putInt(sequenz);
        buffer.putInt(fileId);
        buffer.putLong(0L);
        buffer.putShort(payloadLength);
        buffer.put(payload);
        buffer.putLong(crcPos, makeChecksum(buffer.array()));
        return buffer.array();
    }

    /**
     * Berechnet die Checksumme des BCP-Packets. Über das Array, welches den kompletten Header und den kompletten Payload
     * beinhalten sollte. Und die Bytes, die die Checksumme repräsentieren, sollten auf 0 gesetzt sein.
     * @param data Das Array, über das die Checksumme berechnet werden soll.
     * @return Die berechnete Checksumme des Arrays.
     */
    private long makeChecksum(byte[] data){

        long crc = 0L;
        for (byte b : data) {
            int index = ((int) (crc >>> 56) ^ b) & 0xFF;
            crc = TABLE[index] ^ (crc << 8);
        }
        return crc;
    }

    /**
     * Gibt den Wert zurück, der in dem Versionsfeld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Versionsfeld des Headers steht.
     */
    private byte getVersion(DatagramPacket packet) {
        return packet.getData()[versionPos];
    }

    /**
     * Gibt den Wert zurück, der in dem PayloadLength Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im PayloadLength Feld des Headers steht.
     */
    private short getPayloadLength(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), payloadLengthPos, payloadLengthSize).getShort();
    }


    /**
     * Gibt den Wert zurück, der in dem SrcNodeId Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im SrcNodeId Feld des Headers steht.
     */
    private long getSrcNodeId(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), srcNodeIdPos, srcNodeSize).getLong();
    }

    /**
     * Gibt den Wert zurück, der in dem DestNodeId Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im DestNodeId Feld des Headers steht.
     */
    private long getDestNodeId(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), destNodeIdPos, destNodeSize).getLong();
    }

    /**
     * Gibt den Wert zurück, der in dem Type Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Type Feld des Headers steht.
     */
    private PacketTypes getType(DatagramPacket packet){
        return PacketTypes.values()[ByteBuffer.wrap(packet.getData(), typePos, typeSize).get()];
    }

    /**
     * Gibt den Wert zurück, der in dem TLL Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im TTL Feld des Headers steht.
     */
    private byte getTtl(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), ttlPos, ttlSize).get();
    }

    /**
     * Gibt den Wert zurück, der in dem Hops Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Hops Feld des Headers steht.
     */
    private byte getHops(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), hopsPos, hopsSize).get();
    }

    /**
     * Gibt den Wert zurück, der in dem Sequenz Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Sequenz Feld des Headers steht.
     */
    private int getSequenz(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), sequenzPos, sequenzSize).getInt();
    }

    /**
     * Gibt den Wert zurück, der in dem FileId Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im FileId Feld des Headers steht.
     */
    private int getFileId(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), fileIdPos, fileIdSize).getInt();
    }

    /**
     * Gibt den Wert zurück, der in dem Crc Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Crc Feld des Headers steht.
     */
    private long getCrc(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), crcPos, crcSize).getLong();
    }

    /**
     * Gibt das Array zurück, welches den Payload des BCP-Headers, von dem Packet, darstellt.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Das Array, welches den Payload des Headers darstellt.
     */
    private byte[] getPayload(DatagramPacket packet){
        byte[] payload = new byte[payloadLength];
        System.arraycopy(packet.getData(), payloadPos, payload, 0, payloadLength);
        return payload;
    }


    /**
     * Entfernt die Checksumme aus dem BCP-Header, so dass das Byte-Array danach eine größe von HeaderSize - crcSize hat.
     * Und payloadLength bei CrcPos anfängt.
     * @param header Der Header, von dem die Checksumme entfernt werden soll.
     * @return Ein Byte-Array ohne Checksumme.
     */
    @Deprecated
    private byte[] extractChecksum(byte[] header){
        byte[] headerWithoutChecksum = new byte[headerSize - crcSize];
        System.arraycopy(header, 0, headerWithoutChecksum, 0, headerSize - crcSize - payloadLengthSize);
        headerWithoutChecksum[headerWithoutChecksum.length - payloadLengthSize] = header[payloadLengthPos];
        headerWithoutChecksum[headerWithoutChecksum.length - 1] = header[payloadLengthPos + 1];

        return headerWithoutChecksum;

    }
}


