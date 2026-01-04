package Handlers.ReceiveHandlers;

import ChatAnwendung.Impl.Handler.ReceiverHandlers.RequestRecieveHandler;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.Storage;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class RequestReceiveHandlerTest {
    private static String filePath = "C:\\Users\\leons\\IdeaProjects\\RN-ChatAnwendung\\src\\test\\resources\\TestFiles\\TestFile.jpeg";

    private static int zeile = 0;

    private static Long srcId = 123L;

    private static Integer fileId = 12;

    private long fileSize = new File(filePath).length();

    private static int sequenz = 1;

    private static InetAddress address = InetAddress.getLoopbackAddress();

    private static int port = 5000;

    private static DatagramPacket packet;


    @BeforeEach
    public void createFileAndPacket(){

        Storage.getInstance().setSendOpenFile(fileId, filePath);

        byte[] header = Header.makeHeader(
                (byte) PacketTypes.RESENDREQUEST.ordinal(),
                (byte)32,
                srcId,
                sequenz,
                fileId,
                (short)0,
                new byte[0]

        );

        packet = new DatagramPacket(header, header.length);

        packet.setAddress(address);
        packet.setPort(port);

    }

    @Test
    public void runTest(){

        RequestRecieveHandler handler = new RequestRecieveHandler(packet);
        handler.run();

        DatagramPacket sendPacket;

        try {
            sendPacket = MessageQueue.getInstance().poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(sendPacket);
        assertEquals(address, sendPacket.getAddress());
        assertEquals(port, sendPacket.getPort());

        byte[] data = sendPacket.getData();
        long actualDestId = ByteBuffer.wrap(data, Header.getDestNodePos(), Header.getDestNodeSize()).getLong();
        int actualFileId = ByteBuffer.wrap(data, Header.getFileIdPos(), Header.getFileIdSize()).getInt();
        int actualSequenz = ByteBuffer.wrap(data, Header.getSequenzPos(), Header.getSequenzSize()).getInt();
        int payloadLength = ByteBuffer.wrap(data, Header.getPayloadLengthPos(), Header.getPayloadLengthSize()).getShort();
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, Header.getPayloadPos(), payload, 0, payloadLength);

        assertEquals(Storage.getInstance().getID(), actualDestId);
        assertEquals(fileId, actualFileId);
        assertEquals(sequenz, actualSequenz);

        byte[] expectedPayload = new byte[1300];
        try(RandomAccessFile file = new RandomAccessFile(filePath, "r")){
            file.seek(sequenz * 1300L);
            file.read(expectedPayload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedPayload.length, payload.length);

        for(int i = 0; i < expectedPayload.length; i++){
            assertEquals(expectedPayload[i], payload[i]);
        }
    }

    @Test
    public void runWithLastSequenzTest() throws IOException {

        int sequenz = (int)Math.ceil(fileSize / 1300.0) - 1;

        Header.addInt(Header.getSequenzPos(), sequenz, packet.getData());


        RequestRecieveHandler handler = new RequestRecieveHandler(packet);
        handler.run();

        DatagramPacket sendPacket;

        try {
            sendPacket = MessageQueue.getInstance().poll();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(sendPacket);
        assertEquals(address, sendPacket.getAddress());
        assertEquals(port, sendPacket.getPort());

        byte[] data = sendPacket.getData();
        long actualDestId = ByteBuffer.wrap(data, Header.getDestNodePos(), Header.getDestNodeSize()).getLong();
        int actualFileId = ByteBuffer.wrap(data, Header.getFileIdPos(), Header.getFileIdSize()).getInt();
        int actualSequenz = ByteBuffer.wrap(data, Header.getSequenzPos(), Header.getSequenzSize()).getInt();
        int payloadLength = ByteBuffer.wrap(data, Header.getPayloadLengthPos(), Header.getPayloadLengthSize()).getShort();
        byte[] payload = new byte[payloadLength];
        System.arraycopy(data, Header.getPayloadPos(), payload, 0, payloadLength);

        assertEquals(Storage.getInstance().getID(), actualDestId);
        assertEquals(fileId, actualFileId);
        assertEquals(sequenz, actualSequenz);

        byte[] expectedPayload;
        try(RandomAccessFile file = new RandomAccessFile(filePath, "r")){
            expectedPayload = new byte[(int)file.length() - (sequenz * 1300)];
            file.seek(sequenz * 1300L);
            file.read(expectedPayload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(expectedPayload.length, payload.length);

        for(int i = 0; i < expectedPayload.length; i++){
            assertEquals(expectedPayload[i], payload[i]);
        }
    }
}