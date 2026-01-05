//import ChatAnwendung.Impl.BCPPacket;
//import ChatAnwendung.Impl.PacketTypes;
//import ChatAnwendung.Impl.persistence.Storage;
//import org.junit.jupiter.api.Test;
//
//import java.nio.ByteBuffer;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class BCPPacketTest {
//
//    private static final long POLY = 0x42F0E1EBA9EA3693L;
//    private static final long[] TABLE = new long[256];
//
//    static {
//        for (int i = 0; i < 256; i++) {
//            long c = (long) i << 56;
//            long crc = 0;
//            for (int j = 0; j < 8; j++) {
//                if (((crc ^ c) & 0x8000000000000000L) != 0) {
//                    crc = (crc << 1) ^ POLY;
//                } else {
//                    crc <<= 1;
//                }
//                c <<= 1;
//            }
//            TABLE[i] = crc;
//        }
//    }
//
//
//    @Test
//    public void addLongAtBeginningTest(){
//        long testLong = 12345678910111213L;
//        byte[] array = new byte[Long.BYTES];
//
//        BCPPacket.addLong(0, testLong, array);
//
//        assertEquals(testLong, ByteBuffer.wrap(array).getLong());
//
//    }
//
//    @Test
//    public void addLongAtMiddleTest(){
//        long testLong = 12345678910111213L;
//        byte[] array = new byte[Long.BYTES + 3];
//
//        BCPPacket.addLong(2, testLong, array);
//
//        assertEquals(testLong, ByteBuffer.wrap(array, 2, 8).getLong());
//
//    }
//
//
//    @Test
//    public void addIntAtBeginningTest(){
//        int testInt = 123456789;
//
//        byte[] array = new byte[Integer.BYTES];
//
//        BCPPacket.addInt(0, testInt, array);
//
//        assertEquals(testInt, ByteBuffer.wrap(array).getInt());
//    }
//
//    @Test
//    public void addIntAtMiddleTest(){
//        int testInt = 123456789;
//
//        byte[] array = new byte[Integer.BYTES + 3];
//
//        BCPPacket.addInt(2, testInt, array);
//
//        assertEquals(testInt, ByteBuffer.wrap(array, 2, 4).getInt());
//    }
//
//    @Test
//    public void addShortAtBeginningTest(){
//        short testShort = 12345;
//
//        byte[] array = new byte[Short.BYTES];
//
//        BCPPacket.addShort(0, testShort, array);
//
//        assertEquals(testShort, ByteBuffer.wrap(array).getShort());
//    }
//
//    @Test
//    public void addShortAtMiddleTest(){
//        short testShort = 12345;
//
//        byte[] array = new byte[Short.BYTES + 3];
//
//        BCPPacket.addShort(2, testShort, array);
//
//        assertEquals(testShort, ByteBuffer.wrap(array, 2, 2).getShort());
//    }
//
//    @Test
//    public void extractChecksummTest(){
//        byte version = 1;
//        byte type = (byte) PacketTypes.MESSAGE.ordinal();
//        byte ttl = 32;
//        byte hops = 0;
//        long destId = 1234;
//        long srcId = Storage.getInstance().getID();
//        int sequenz = 0;
//        int fileId = 0;
//        byte[] payload = new byte[] {41, 56, 43, 68};
//        short payloadLength = (short) payload.length;
//
//        byte[] header = BCPPacket.makeHeader(
//                type,
//                ttl,
//                destId,
//                sequenz,
//                fileId,
//                payloadLength,
//                payload
//        );
//
//        byte[] headerWithoutChecksumm = BCPPacket.extractChecksum(header);
//
//        assertEquals(30, headerWithoutChecksumm.length);
//        assertEquals(version, ByteBuffer.wrap(headerWithoutChecksumm, 0, 1).get());
//        assertEquals(type, ByteBuffer.wrap(headerWithoutChecksumm, 1, 1).get());
//        assertEquals(ttl, ByteBuffer.wrap(headerWithoutChecksumm, 2, 1).get());
//        assertEquals(hops, ByteBuffer.wrap(headerWithoutChecksumm, 3, 1).get());
//        assertEquals(srcId, ByteBuffer.wrap(headerWithoutChecksumm, 4, 8).getLong());
//        assertEquals(destId, ByteBuffer.wrap(headerWithoutChecksumm, 12, 8).getLong());
//        assertEquals(sequenz, ByteBuffer.wrap(headerWithoutChecksumm, 20, 4).getInt());
//        assertEquals(fileId, ByteBuffer.wrap(headerWithoutChecksumm, 24, 4).getInt());
//        assertEquals(payloadLength, ByteBuffer.wrap(headerWithoutChecksumm, 28, 2).getShort());
//    }
//
//    @Test
//    public void makeChekcsummTest(){
//        byte[] test = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
//
//        long crc = 0L;
//        for (byte b : test) {
//            int index = ((int) (crc >>> 56) ^ b) & 0xFF;
//            crc = TABLE[index] ^ (crc << 8);
//        }
//
//        long actualCrc = BCPPacket.makeChecksum(test);
//
//        assertEquals(crc , actualCrc);
//
//    }
//
//    @Test
//    public void makeHeaderTest(){
//        byte version = 1;
//        byte type = (byte) PacketTypes.MESSAGE.ordinal();
//        byte ttl = 32;
//        byte hops = 0;
//        long destId = 1234;
//        long srcId = Storage.getInstance().getID();
//        int sequenz = 0;
//        int fileId = 0;
//        byte[] payload = new byte[] {41, 56, 43, 68};
//        short payloadLength = (short) payload.length;
//
//        byte[] header = BCPPacket.makeHeader(
//                type,
//                ttl,
//                destId,
//                sequenz,
//                fileId,
//                payloadLength,
//                payload
//        );
//
//        assertEquals(version, ByteBuffer.wrap(header, 0, 1).get());
//        assertEquals(type, ByteBuffer.wrap(header, 1, 1).get());
//        assertEquals(ttl, ByteBuffer.wrap(header, 2, 1).get());
//        assertEquals(hops, ByteBuffer.wrap(header, 3, 1).get());
//        assertEquals(srcId, ByteBuffer.wrap(header, 4, 8).getLong());
//        assertEquals(destId, ByteBuffer.wrap(header, 12, 8).getLong());
//        assertEquals(sequenz, ByteBuffer.wrap(header, 20, 4).getInt());
//        assertEquals(fileId, ByteBuffer.wrap(header, 24, 4).getInt());
//        assertEquals(payloadLength, ByteBuffer.wrap(header, 36, 2).getShort());
//
//        byte[] headerWithoutChecksumm = BCPPacket.extractChecksum(header);
//
//        byte[] checksummArray = new byte[headerWithoutChecksumm.length + payloadLength];
//        System.arraycopy(headerWithoutChecksumm, 0, checksummArray, 0, headerWithoutChecksumm.length);
//        System.arraycopy(payload, 0, checksummArray, headerWithoutChecksumm.length, payloadLength);
//
//        long crc = 0L;
//        for (byte b : checksummArray) {
//            int index = ((int) (crc >>> 56) ^ b) & 0xFF;
//            crc = TABLE[index] ^ (crc << 8);
//        }
//
//        long actualCrc = BCPPacket.makeChecksum(checksummArray);
//
//        assertEquals(crc, actualCrc);
//    }
//}
