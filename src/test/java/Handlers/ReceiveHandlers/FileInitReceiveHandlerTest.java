package Handlers.ReceiveHandlers;

import ChatAnwendung.Impl.Handler.ReceiverHandlers.FileDataRecieveHandler;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.FileInitReceiveHandler;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.Storage;
import ChatAnwendung.Impl.persistence.ThreadPools;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileInitReceiveHandlerTest {

    private static String filePath = "C:\\Users\\leons\\Desktop\\testFile.txt";

    @AfterEach
    public void clean(){
        DownloadFiles.getInstance().removeAll();
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void shutdown(){
        ThreadPools.getInstance().shutDown();

    }
    @Test
    public void runTest(){

        String name = filePath;
        byte[] nameBytes = name.getBytes();
        int fileId = 24;
        long srcId = 123;
        int sequenz = 1;
        int fileSize = 30;
        short payloadLength = (short) (nameBytes.length + 4);
        byte[] packetBytes = new byte[Header.getHeaderSize() + nameBytes.length + 4];
        Header.addLong(Header.getSrcNodePos(), srcId, packetBytes);
        Header.addInt(Header.getFileIdPos(), fileId, packetBytes);
        Header.addInt(Header.getSequenzPos(), sequenz, packetBytes);
        Header.addShort(Header.getPayloadLengthPos(), payloadLength, packetBytes);
        Header.addInt(Header.getPayloadPos(), fileSize, packetBytes);
        System.arraycopy(nameBytes, 0, packetBytes, Header.getPayloadPos() + 4, nameBytes.length);

        DatagramPacket packet = new DatagramPacket(packetBytes, packetBytes.length);

        FileInitReceiveHandler handler = new FileInitReceiveHandler(packet);
        handler.run();

        assertNotNull(DownloadFiles.getInstance().getFile(srcId, fileId));
        assertEquals(name, DownloadFiles.getInstance().getFile(srcId, fileId).getName());
        assertEquals(fileId, DownloadFiles.getInstance().getFile(srcId, fileId).getFileId());
        assertEquals(srcId, DownloadFiles.getInstance().getFile(srcId, fileId).getSrcUID());
        assertEquals(0, DownloadFiles.getInstance().getFile(srcId, fileId).getNextNeededChunk());
    }


    @Test
    public void runWithFileDataFirstTest(){
        int fileId = 23;
        long srcId = 123;
        int fileDataSequenz = 0;
        byte[] fileDataPayload = new byte[] {97};
        short fileDataPayloadLength = (short) fileDataPayload.length;
        byte[] fileDataBytes = new byte[Header.getHeaderSize() + fileDataPayloadLength];


        Header.addLong(Header.getSrcNodePos(), srcId, fileDataBytes);
        Header.addInt(Header.getFileIdPos(), fileId, fileDataBytes);
        Header.addInt(Header.getSequenzPos(), fileDataSequenz, fileDataBytes);
        Header.addShort(Header.getPayloadLengthPos(), fileDataPayloadLength, fileDataBytes);
        System.arraycopy(fileDataPayload, 0, fileDataBytes, Header.getPayloadPos(), fileDataPayloadLength);

        DatagramPacket fileDataPacket = new DatagramPacket(fileDataBytes, fileDataBytes.length);

        CompletableFuture<Void> fileData = CompletableFuture.runAsync(new FileDataRecieveHandler(fileDataPacket));

        String name = filePath;
        byte[] nameBytes = name.getBytes();
        int fileInitSequenz = 10;
        int fileSize = 30;
        short fileInitPayloadLength = (short) (nameBytes.length + 4);
        byte[] fileInitByte = new byte[Header.getHeaderSize() + nameBytes.length + 4];

        Header.addLong(Header.getSrcNodePos(), srcId, fileInitByte);
        Header.addInt(Header.getFileIdPos(), fileId, fileInitByte);
        Header.addInt(Header.getSequenzPos(), fileInitSequenz, fileInitByte);
        Header.addShort(Header.getPayloadLengthPos(), fileInitPayloadLength, fileInitByte);
        Header.addInt(Header.getPayloadPos(), fileSize, fileInitByte);
        System.arraycopy(nameBytes, 0, fileInitByte, Header.getPayloadPos() + 4, nameBytes.length);

        DatagramPacket fileInitPacket = new DatagramPacket(fileInitByte, fileInitByte.length);

        FileInitReceiveHandler handler = new FileInitReceiveHandler(fileInitPacket);
        handler.run();

        assertNotNull(DownloadFiles.getInstance().getFile(srcId, fileId));
        assertEquals(name, DownloadFiles.getInstance().getFile(srcId, fileId).getName());
        assertEquals(fileId, DownloadFiles.getInstance().getFile(srcId, fileId).getFileId());
        assertEquals(srcId, DownloadFiles.getInstance().getFile(srcId, fileId).getSrcUID());
        fileData.join();
        assertEquals(0, DownloadFiles.getInstance().getFile(srcId, fileId).getNextNeededChunk());
        DownloadFiles.getInstance().removeFile(srcId, fileId);
    }
}
