package Handlers.ReceiveHandlers;

import ChatAnwendung.Impl.Handler.ReceiverHandlers.FileDataRecieveHandler;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.FileInitReceiveHandler;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.DownloadFiles;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileInitReceiveHandlerTest {

    private int fileId = 24;
    private long srcId = 123;
    private int sequenz = 0;
    private int fileSize = 30;

    private static final String  fileName = "FileInitTestFile.txt";

    private final byte[] payload  = new byte[fileName.getBytes().length + 4];

    private DatagramPacket packet;

    @BeforeEach
    public void createPacket(){

        Header.addInt(0, fileSize, payload);
        System.arraycopy(fileName.getBytes(), 0, payload, 4, fileName.getBytes().length);

        byte[] header = Header.makeHeader(
                (byte) PacketTypes.FILE_INIT.ordinal(),
                (byte) 32,
                srcId,
                1,
                fileId,
                (short)payload.length,
                payload
        );

        Header.addLong(Header.getSrcNodePos(), srcId, header);

        byte[] packetBytes = new byte[header.length + payload.length];

        System.arraycopy(header, 0, packetBytes, 0, header.length);
        System.arraycopy(payload, 0, packetBytes, header.length, payload.length);

        packet = new DatagramPacket(packetBytes, packetBytes.length);
    }

    @AfterEach
    public void clean(){
        DownloadFiles.getInstance().removeAll();
        try {
            Files.delete(Paths.get(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void shutdown(){
        try {
            Files.delete(Paths.get(fileName));
        }catch (NoSuchFileException e){

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Test
    public void runTest(){
        FileInitReceiveHandler handler = new FileInitReceiveHandler(packet);
        handler.run();

        assertNotNull(DownloadFiles.getInstance().getFile(srcId, fileId));
        assertEquals(fileName, DownloadFiles.getInstance().getFile(srcId, fileId).getName());
        assertEquals(fileId, DownloadFiles.getInstance().getFile(srcId, fileId).getFileId());
        assertEquals(srcId, DownloadFiles.getInstance().getFile(srcId, fileId).getSrcUID());
        assertEquals(0, DownloadFiles.getInstance().getFile(srcId, fileId).getNextNeededChunk());
    }


    @Test
    public void runWithFileDataFirstTest(){

        byte[] fileDataPayload = new byte[] {97,98,99,100,96,95,94,93,92,91,90,97,97,97,97,97,97,97,97,97,
                97,97,97,97,97,97,102,97,97,97,};
        short fileDataPayloadLength = (short) fileDataPayload.length;
        byte[] fileDataBytes = new byte[Header.getHeaderSize() + fileDataPayloadLength];


        Header.addLong(Header.getSrcNodePos(), srcId, fileDataBytes);
        Header.addInt(Header.getFileIdPos(), fileId, fileDataBytes);
        Header.addInt(Header.getSequenzPos(), sequenz, fileDataBytes);
        Header.addShort(Header.getPayloadLengthPos(), fileDataPayloadLength, fileDataBytes);
        System.arraycopy(fileDataPayload, 0, fileDataBytes, Header.getPayloadPos(), fileDataPayloadLength);

        DatagramPacket fileDataPacket = new DatagramPacket(fileDataBytes, fileDataBytes.length);

        CompletableFuture<Void> fileData = CompletableFuture.runAsync(new FileDataRecieveHandler(fileDataPacket));



        FileInitReceiveHandler handler = new FileInitReceiveHandler(packet);
        handler.run();

        assertNotNull(DownloadFiles.getInstance().getFile(srcId, fileId));
        assertEquals(fileName, DownloadFiles.getInstance().getFile(srcId, fileId).getName());
        assertEquals(fileId, DownloadFiles.getInstance().getFile(srcId, fileId).getFileId());
        assertEquals(srcId, DownloadFiles.getInstance().getFile(srcId, fileId).getSrcUID());
        fileData.join();
        assertEquals(0, DownloadFiles.getInstance().getFile(srcId, fileId).getNextNeededChunk());
        DownloadFiles.getInstance().removeFile(srcId, fileId);
    }
}
