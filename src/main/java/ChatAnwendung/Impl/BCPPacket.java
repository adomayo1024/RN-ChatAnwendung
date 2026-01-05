package ChatAnwendung.Impl;

import ChatAnwendung.Impl.persistence.Storage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

@Setter
@Slf4j
@AllArgsConstructor
public class BCPPacket {

    private static final long POLY = 0x42F0E1EBA9EA3693L;
    private static final long[] TABLE = new long[256];

    @Getter
    private static final int headerSize = 38;
    @Getter
    private static final int versionSize = 1;
    @Getter
    private static final int typeSize = 1;
    @Getter
    private static final int ttlSize = 1;
    @Getter
    private static final int hopsSize = 1;
    @Getter
    private static final int srcNodeSize = 8;
    @Getter
    private static final int destNodeSize = 8;
    @Getter
    private static final int sequenzSize = 4;
    @Getter
    private static final int fileIdSize = 4;
    @Getter
    private static final int crcSize = 8;
    @Getter
    private static final int payloadLengthSize = 2;
    @Getter
    private static final int versionPos = 0;
    @Getter
    private static final int typePos = versionPos + versionSize;
    @Getter
    private static final int ttlPos = typePos + typeSize;
    @Getter
    private static final int hopsPos = ttlPos + ttlSize;
    @Getter
    private static final int srcNodeIdPos = hopsPos + hopsSize;
    @Getter
    private static final int destNodeIdPos = srcNodeIdPos + srcNodeSize;
    @Getter
    private static final int sequenzPos = destNodeIdPos + destNodeSize;
    @Getter
    private static final int fileIdPos = sequenzPos + sequenzSize;
    @Getter
    private static final int crcPos = fileIdPos + fileIdSize;
    @Getter
    private static final int payloadLengthPos = crcPos + crcSize;
    @Getter
    private static final int payloadPos = payloadLengthPos + payloadLengthSize;
    @Getter
    private static final int maximumFileSendingChunkSize = 1300;



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




    public  byte[] makeHeader(byte type, byte ttl, long destId, int sequenz, int fileId, short payloadLength, byte[] payload) {
        byte version = 1;
        byte hops = 0;
        long srcID = this.srcNodeId;


        byte[] header = new byte[headerSize];
        byte[] packet = new byte[payloadLength + headerSize];

        header[versionPos] = version;
        header[typePos] = type;
        header[ttlPos] = ttl;
        header[hopsPos] = hops;
        addLong(srcNodeIdPos, srcID, header);
        addLong(destNodeIdPos, destId, header);
        addInt(sequenzPos, sequenz, header);
        addInt(fileIdPos, fileId, header);
        addShort(payloadLengthPos, payloadLength, header);
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(payload, 0, packet, headerSize, payloadLength);
        addLong(crcPos, makeChecksum(extractChecksum(packet)), header);

        return header;
    }

    public static long makeChecksum(byte[] data){

        long crc = 0L;
        for (byte b : data) {
            int index = ((int) (crc >>> 56) ^ b) & 0xFF;
            crc = TABLE[index] ^ (crc << 8);
        }
        return crc;
    }

    public static byte[] extractChecksum(byte[] header){
        byte[] headerWithoutChecksum = new byte[headerSize - crcSize];
        System.arraycopy(header, 0, headerWithoutChecksum, 0, headerSize - crcSize - payloadLengthSize);
        headerWithoutChecksum[headerWithoutChecksum.length - payloadLengthSize] = header[payloadLengthPos];
        headerWithoutChecksum[headerWithoutChecksum.length - 1] = header[payloadLengthPos + 1];

        return headerWithoutChecksum;

    }

    public static void addLong(int pos, long value, byte[] array) {
        array[pos] = (byte) (value >> 56);
        array[pos + 1] = (byte) (value >> 48);
        array[pos + 2] = (byte) (value >> 40);
        array[pos + 3] = (byte) (value >> 32);
        array[pos + 4] = (byte) (value >> 24);
        array[pos + 5] = (byte) (value >> 16);
        array[pos + 6] = (byte) (value >> 8);
        array[pos + 7] = (byte) (value);

    }

    public static void addInt(int pos, int value, byte[] array) {
        array[pos] = (byte) (value >> 24);
        array[pos + 1] = (byte) (value >> 16);
        array[pos + 2] = (byte) (value >> 8);
        array[pos + 3] = (byte) (value);
    }

    public static void addShort(int pos, short value, byte[] array){
        array[pos] = (byte) (value >> 8);
        array[pos + 1] = (byte) (value);
    }

    @Getter
    private byte version;

    @Getter
    private PacketTypes type;

    @Getter
    private byte ttl;

    @Getter
    private byte hops;

    @Getter
    private long srcNodeId;

    @Getter
    private long destNodeId;

    @Getter
    private int sequenz;

    @Getter
    private int fileId;

    @Getter
    private long crc;

    @Getter
    private short payloadLength;

    @Getter
    private byte[] payload;

    @Getter
    private InetAddress address;

    @Getter
    private int port;

    public BCPPacket(DatagramPacket packet){
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

        return new DatagramPacket(packet, packet.length);

    }

    private byte getVersion(DatagramPacket packet) {
        return packet.getData()[versionPos];
    }

    private short getPayloadLength(DatagramPacket packet){
        return makeBytesToShort(packet.getData(), payloadLengthPos);
    }

    private long getSrcNodeId(DatagramPacket packet){
        return makeBytesToLong(packet.getData(), srcNodeIdPos);
    }

    private long getDestNodeId(DatagramPacket packet){
        return makeBytesToLong(packet.getData(), destNodeIdPos);
    }

    private PacketTypes getType(DatagramPacket packet){
        return PacketTypes.values()[packet.getData()[typePos]];
    }

    private byte getTtl(DatagramPacket packet){
        return packet.getData()[ttlPos];
    }

    private byte getHops(DatagramPacket packet){
        return packet.getData()[hopsPos];
    }

    private int getSequenz(DatagramPacket packet){
        return makeBytesToInt(packet.getData(), sequenzPos);
    }

    private int getFileId(DatagramPacket packet){
        return makeBytesToInt(packet.getData(), fileIdPos);
    }

    private long getCrc(DatagramPacket packet){
        return makeBytesToLong(packet.getData(), crcPos);
    }

    private byte[] getPayload(DatagramPacket packet){
        byte[] payload = new byte[payloadLength];
        System.arraycopy(packet.getData(), payloadPos, payload, 0, payloadLength);
        return payload;
    }

    public String getFileName(){

        if(type != PacketTypes.FILE_INIT){
            return null;
        }

        byte[] name = new byte[payloadLength - 4];

        for(int i = 0; i < name.length; i++){
            name[i] = payload[i + 4];
        }

        return new String(name, StandardCharsets.UTF_8);
    }

    public int getFileSize(){
        if(type != PacketTypes.FILE_INIT){
            return -1;
        }

        return makeBytesToInt(payload, 0);
    }



    private long makeBytesToLong(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];
        byte b2 = data[pos + 2];
        byte b3 = data[pos + 3];
        byte b4 = data[pos + 4];
        byte b5 = data[pos + 5];
        byte b6 = data[pos + 6];
        byte b7 = data[pos + 7];

        return ((long)(b0 & 0xFF) << 56) |
                ((long)(b1 & 0xFF) << 48) |
                ((long)(b2 & 0xFF) << 40) |
                ((long)(b3 & 0xFF) << 32) |
                ((long)(b4 & 0xFF) << 24) |
                ((long)(b5 & 0xFF) << 16) |
                ((long)(b6 & 0xFF) <<  8) |
                ((long)(b7 & 0xFF));
    }


    private short makeBytesToShort(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];

        return (short) (((b0 & 0xFF) << 8) | (b1 & 0xFF));
    }

    private int makeBytesToInt(byte[] data, int pos){
        byte b0 = data[pos];
        byte b1 = data[pos + 1];
        byte b2 = data[pos + 2];
        byte b3 = data[pos + 3];

        return ((b0 & 0xFF) << 24) |
                ((b1 & 0xFF) << 16) |
                ((b2 & 0xFF) << 8) |
                (b3 & 0xFF);
    }

    public long getNodeIdFromRoutingTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }
        return makeBytesToLong(payload, offset);
    }

    public long getLastSeenFromRoutinTableEntry(int offset) {
        if(type != PacketTypes.ROUTINGTABLE){
            return -1;
        }

        return makeBytesToLong(payload, offset + destNodeSize + hopsSize);
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
        return destNodeId == myId;
    }

    public long calculateCrc() {

        return makeBytesToLong(makeBCPPacketIntoBytes(), crcPos);
    }

    private byte[] makeBCPPacketIntoBytes(){
        byte[] header = new byte[headerSize];
        byte[] packet = new byte[payloadLength + headerSize];

        header[versionPos] = version;
        header[typePos] = (byte)type.ordinal();
        header[ttlPos] = ttl;
        header[hopsPos] = hops;
        addLong(srcNodeIdPos, srcNodeId, header);
        addLong(destNodeIdPos, destNodeId, header);
        addInt(sequenzPos, sequenz, header);
        addInt(fileIdPos, fileId, header);
        addShort(payloadLengthPos, payloadLength, header);

        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(payload, 0, packet, headerSize, payloadLength);
        addLong(crcPos, makeChecksum(extractChecksum(packet)), packet);
        return packet;
    }
}
