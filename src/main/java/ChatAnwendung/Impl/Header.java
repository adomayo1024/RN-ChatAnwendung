package ChatAnwendung.Impl;

import ChatAnwendung.Impl.persistence.Storage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Header {

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
    private static final int srcNodePos = hopsPos + hopsSize;
    @Getter
    private static final int destNodePos = srcNodePos + srcNodeSize;
    @Getter
    private static final int sequenzPos = destNodePos + destNodeSize;
    @Getter
    private static final int fileIdPos = sequenzPos + sequenzSize;
    @Getter
    private static final int crcPos = fileIdPos + fileIdSize;
    @Getter
    private static final int payloadLengthPos = crcPos + crcSize;
    @Getter
    private static final int payloadPos = payloadLengthPos + payloadLengthSize;



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
}
