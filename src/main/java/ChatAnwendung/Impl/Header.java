package ChatAnwendung.Impl;

public class Header {

    private static final long POLY = 0x42F0E1EBA9EA3693L;
    private static final long[] TABLE = new long[256];

    private static final int versionPos = 0;
    private static final int typePos = 1;
    private static final int ttlPos = 2;
    private static final int hopsPos = 3;
    private static final int srcNodePos = 4;
    private static final int destNodePos = 12;
    private static final int sequenzPos = 20;
    private static final int fileIdPos = 24;
    private static final int crcPos = 28;
    private static final int payloadLenghtPos = 36;
    private static final int payloadPos = 38;



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


    public static byte[] makeHeader(byte type, byte ttl, long destId, int sequenz, int fileId, short payloadLenght) {
        byte version = 1;
        byte hops = 0;
        long srcID = Storage.getInstance().getID();


        byte[] header = new byte[38];

        header[versionPos] = version;
        header[typePos] = type;
        header[ttlPos] = ttl;
        header[hopsPos] = hops;
        addLong(srcNodePos, srcID, header);
        addLong(destNodePos, destId, header);
        addInt(sequenzPos, sequenz, header);
        addInt(fileIdPos, fileId, header);
        addShort(payloadLenghtPos, payloadLenght, header);
        addLong(crcPos, makeChecksumm(header), header);

        return header;
    }

    public static long makeChecksumm(byte[] header){

        long crc = 0xFFFFFFFFFFFFFFFFL; // Initialwert für CRC64-ECMA
        for (byte b : header) {
            int idx = ((int) crc ^ (b & 0xFF)) & 0xFF;
            crc = TABLE[idx] ^ (crc >>> 8);
        }

        return ~crc;
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

    public static int getPayloadLenghtPos(){
        return payloadLenghtPos;
    }

    public static int getPayloadPos(){
        return payloadPos;
    }

}
