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

        byte[] name = new byte[payloadLength - BCPPacket.FILE_INIT_FILE_SIZE_SIZE];
        System.arraycopy(payload, BCPPacket.ROUTING_TABLE_FILE_NAME_POS, name, 0, name.length);

        return new String(name, StandardCharsets.UTF_8);
    }

    public int getFileSize(){
        if(type != PacketTypes.FILE_INIT){
            return -1;
        }

        return ByteBuffer.wrap(payload, BCPPacket.FILE_INIT_FILE_SIZE_POS, BCPPacket.FILE_INIT_FILE_SIZE_SIZE).getInt();
    }

    public long getNodeIdFromRoutingTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }

        return ByteBuffer.wrap(payload, offset, BCPPacket.ROUTING_TABLE_DEST_NODE_ID_SIZE).getLong();

    }

    public long getLastSeenFromRoutingTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }

        return ByteBuffer.wrap(payload, offset + BCPPacket.ROUTING_TABLE_LAST_SEEN_POS, BCPPacket.ROUTING_TABLE_LAST_SEEN_SIZE).getLong();
    }

    public byte getHopsFromRoutingTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }
        return payload[offset + BCPPacket.DEST_NODE_SIZE];
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

        long calculateCrc = ByteBuffer.wrap(data, BCPPacket.CRC_POS, BCPPacket.CRC_SIZE).getLong();

        crc = tempCrc;

        return calculateCrc;
    }

    /**
     * Erstellt ein Byte-Array aus dem BCP-Packet.
     * @return Das Byte-Array, welches das BCP-Packet darstellt.
     */
    private byte[] makeBCPPacketIntoBytes(){

        ByteBuffer buffer = ByteBuffer.allocate(payloadLength + BCPPacket.HEADER_SIZE);
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
        buffer.putLong(BCPPacket.CRC_POS, makeChecksum(buffer.array()));
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
        return packet.getData()[BCPPacket.VERSION_POS];
    }

    /**
     * Gibt den Wert zurück, der in dem PayloadLength Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im PayloadLength Feld des Headers steht.
     */
    private short getPayloadLength(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.PAYLOAD_LENGTH_POS, BCPPacket.PAYLOAD_LENGTH_SIZE).getShort();
    }


    /**
     * Gibt den Wert zurück, der in dem SrcNodeId Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im SrcNodeId Feld des Headers steht.
     */
    private long getSrcNodeId(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.SRC_NODE_ID_POS, BCPPacket.SRC_NODE_SIZE).getLong();
    }

    /**
     * Gibt den Wert zurück, der in dem DestNodeId Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im DestNodeId Feld des Headers steht.
     */
    private long getDestNodeId(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.DEST_NODE_ID_POS, BCPPacket.DEST_NODE_SIZE).getLong();
    }

    /**
     * Gibt den Wert zurück, der in dem Type-Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Type Feld des Headers steht.
     */
    private PacketTypes getType(DatagramPacket packet){
        return PacketTypes.values()[ByteBuffer.wrap(packet.getData(), BCPPacket.TYPE_POS, BCPPacket.TYPE_SIZE).get()];
    }

    /**
     * Gibt den Wert zurück, der in dem TLL Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im TTL Feld des Headers steht.
     */
    private byte getTtl(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.TTL_POS, BCPPacket.TTL_SIZE).get();
    }

    /**
     * Gibt den Wert zurück, der in dem Hops Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Hops Feld des Headers steht.
     */
    private byte getHops(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.HOPS_POS, BCPPacket.HOPS_SIZE).get();
    }

    /**
     * Gibt den Wert zurück, der in dem Sequenzfeld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Sequenz Feld des Headers steht.
     */
    private int getSequenz(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.SEQUENZ_POS, BCPPacket.SEQUENZ_SIZE).getInt();
    }

    /**
     * Gibt den Wert zurück, der in dem FileId Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im FileId Feld des Headers steht.
     */
    private int getFileId(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.FILE_ID_POS, BCPPacket.FILE_ID_SIZE).getInt();
    }

    /**
     * Gibt den Wert zurück, der in dem Crc Feld des BCP-Headers, von dem Packet, steht.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Der Wert, der im Crc Feld des Headers steht.
     */
    private long getCrc(DatagramPacket packet){
        return ByteBuffer.wrap(packet.getData(), BCPPacket.CRC_POS, BCPPacket.CRC_SIZE).getLong();
    }

    /**
     * Gibt das Array zurück, welches den Payload des BCP-Headers, von dem Packet, darstellt.
     * Wenn das Packet kein BCP-Packet darstellt, gibt es irgendetwas zurück.
     * @param packet Das Datagram Packet, welches ein BCP-Packet darstellt.
     * @return Das Array, welches den Payload des Headers darstellt.
     */
    private byte[] getPayload(DatagramPacket packet){
        byte[] payload = new byte[payloadLength];
        System.arraycopy(packet.getData(), BCPPacket.PAYLOAD_POS, payload, 0, payloadLength);
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
        byte[] headerWithoutChecksum = new byte[BCPPacket.HEADER_SIZE - BCPPacket.CRC_SIZE];
        System.arraycopy(header, 0, headerWithoutChecksum, 0, BCPPacket.HEADER_SIZE - BCPPacket.CRC_SIZE - BCPPacket.PAYLOAD_LENGTH_SIZE);
        headerWithoutChecksum[headerWithoutChecksum.length - BCPPacket.PAYLOAD_LENGTH_SIZE] = header[BCPPacket.PAYLOAD_LENGTH_POS];
        headerWithoutChecksum[headerWithoutChecksum.length - 1] = header[BCPPacket.PAYLOAD_LENGTH_POS + 1];

        return headerWithoutChecksum;

    }
}


