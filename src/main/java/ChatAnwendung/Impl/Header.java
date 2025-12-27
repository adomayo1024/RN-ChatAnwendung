package ChatAnwendung.Impl;

import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Header {

    private static final long POLY = 0x42F0E1EBA9EA3693L;
    private static final long[] TABLE = new long[256];

    private static final int headerSize = 38;
    private static final int versionSize = 1;
    private static final int typeSize = 1;
    private static final int ttlSize = 1;
    private static final int hopsSize = 1;
    private static final int srcNodeSize = 8;
    private static final int destNodeSize = 8;
    private static final int sequenzSize = 4;
    private static final int fileIdSize = 4;
    private static final int crcSize = 8;
    private static final int payloadLengthSize = 2;
    private static final int versionPos = 0;
    private static final int typePos = versionPos + versionSize;
    private static final int ttlPos = typePos + typeSize;
    private static final int hopsPos = ttlPos + ttlSize;
    private static final int srcNodePos = hopsPos + hopsSize;
    private static final int destNodePos = srcNodePos + srcNodeSize;
    private static final int sequenzPos = destNodePos + destNodeSize;
    private static final int fileIdPos = sequenzPos + sequenzSize;
    private static final int crcPos = fileIdPos + fileIdSize;
    private static final int payloadLengthPos = crcPos + crcSize;
    private static final int payloadPos = payloadLengthPos + payloadLengthSize;



    static {
        for (int i = 0; i < 256; i++) {
            long crc = i;
            for (int j = 0; j < 8; j++) {
                if ((crc & 1) != 0) {
                    crc = (crc >>> 1) ^ POLY;
                } else {
                    crc >>>= 1;
                }
            }
            TABLE[i] = crc;
        }
    }


    public static byte[] makeHeader(byte type, byte ttl, long destId, int sequenz, int fileId, short payloadLength, byte[] payload) {
        byte version = 1;
        byte hops = 0;
        long srcID = Storage.getInstance().getID();


        byte[] header = new byte[headerSize];
        byte[] packet = new byte[payloadLength + headerSize];

        header[versionPos] = version;
        header[typePos] = type;
        header[ttlPos] = ttl;
        header[hopsPos] = hops;
        addLong(srcNodePos, srcID, header);
        addLong(destNodePos, destId, header);
        addInt(sequenzPos, sequenz, header);
        addInt(fileIdPos, fileId, header);
        addShort(payloadLengthPos, payloadLength, header);
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(payload, 0, packet, headerSize, payloadLength);
        addLong(crcPos, makeChecksum(extractChecksum(packet)), header);

        return header;
    }

    public static long makeChecksum(byte[] data){

        long crc = 0xFFFFFFFFFFFFFFFFL; // Initialwert für CRC64-ECMA
        for (byte b : data) {
            int idx = ((int) crc ^ (b & 0xFF)) & 0xFF;
            crc = TABLE[idx] ^ (crc >>> 8);
        }

        return ~crc;
    }

    public static byte[] extractChecksum(byte[] header){
        byte[] headerWithoutChecksum = new byte[headerSize - crcSize];
        System.arraycopy(header, 0, headerWithoutChecksum, 0, headerSize - crcSize - payloadLengthSize);
        headerWithoutChecksum[headerWithoutChecksum.length - payloadLengthSize] = header[payloadLengthPos + 1];
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


    //----------------------GETTER-------------------------

    public static int getVersionPos(){
        return versionPos;
    }

    public static int getTypePos(){
        return typePos;
    }

    public static int getTtlPos(){
        return ttlPos;
    }

    public static int getHopsPos(){
        return hopsPos;
    }

    public static int getSrcNodePos(){
        return srcNodePos;
    }

    public static int getDestNodePos(){
        return destNodePos;
    }

    public static int getSequenzPos(){
        return sequenzPos;
    }

    public static int getFileIdPos(){
        return fileIdPos;
    }

    public static int getCrcPos(){
        return crcPos;
    }

    public static int getPayloadLengthPos(){
        return payloadLengthPos;
    }

    public static int getPayloadPos(){
        return payloadPos;
    }

}
