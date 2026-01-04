package Handlers.ReceiveHandlers;

import ChatAnwendung.Impl.Handler.ReceiverHandlers.FileDataRecieveHandler;
import ChatAnwendung.Impl.BCPPacket;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.File;
import ChatAnwendung.Impl.persistence.ThreadPools;
import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FileDataReceiverHanlderTest {


    private final int fileId = 12;

    private final long srcID = 123;

    private final int sequenz = 0;

    private final int middleSequenz = 12;

    private  byte[] payload = new byte[1300];

    private  short payloadlenght = (short)payload.length;

    private static final String filePathLin = "/home/adomayo1024/IdeaProjects/ChatAnwendung-RN/src/test/resources/TestFiles/TestFile.jpeg";

    private static final String filePathWin = "C:\\Users\\leons\\IdeaProjects\\RN-ChatAnwendung\\src\\test\\resources\\TestFiles\\TestFile.jpeg";

    private static   String newFilePath;

    private  DatagramPacket packet;

    @BeforeAll
    public static void createNewFilePath(){

        String filePath = System.getProperty("os.name").toLowerCase().contains("win") ? filePathWin : filePathLin;
            String[] filePathSplit= filePath.split("\\.");
            newFilePath = filePathSplit[0];
            newFilePath += "test";
            newFilePath += ".";
            newFilePath += filePathSplit[1];
    }

    @BeforeEach
    public void createFileAndPacket()  {



        Path source = System.getProperty("os.name").toLowerCase().contains("win") ? Paths.get(filePathWin) : Paths.get(filePathLin);
        Path target = Path.of(newFilePath);

        try {
            Files.createFile(target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        long fileSIze;

        try {
            fileSIze = Files.size(source);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        int anzahlChunks = (int) Math.ceil(fileSIze / 1300.0);

        File file = new File(anzahlChunks, (int)fileSIze, newFilePath, fileId, srcID, ThreadPools.getInstance().getFileRequestTimer());

        DownloadFiles.getInstance().setNewFile(srcID, fileId, file);

        file.startRequesting();

        try(RandomAccessFile randFile = new RandomAccessFile(newFilePath, "r")){
            randFile.seek(sequenz * 1300);
            randFile.read(payload);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        byte[] header = BCPPacket.makeHeader(
                (byte) PacketTypes.FILE_DATA.ordinal(),
                (byte)32,
                srcID,
                sequenz,
                fileId,
                (short)0,
                payload

        );

        BCPPacket.addLong(BCPPacket.getSrcNodeIdPos(), srcID, header);

        byte[] packetBytes = new byte[header.length + payloadlenght];

        System.arraycopy(header, 0, packetBytes, 0, header.length);
        System.arraycopy(payload, 0, packetBytes, header.length, payloadlenght);

        packet = new DatagramPacket(packetBytes, packetBytes.length);
    }

    @AfterEach
    public void deleteFile(){
        DownloadFiles.getInstance().removeAll();
        try {
            Files.delete(Paths.get(newFilePath));
        } catch(NoSuchFileException | DirectoryNotEmptyException e){

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void shutdown(){
        DownloadFiles.getInstance().removeAll();
        try {
            Files.delete(Paths.get(newFilePath));
        } catch(NoSuchFileException | DirectoryNotEmptyException e){

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void runBeginningSequenzTest(){
        FileDataRecieveHandler handler = new FileDataRecieveHandler(packet);
        handler.run();

        assertEquals(1, DownloadFiles.getInstance().getFile(srcID, fileId).getNextNeededChunk());

        byte[] expectedPayload = new byte[payloadlenght];
        try(RandomAccessFile file = new RandomAccessFile(newFilePath, "r")){
            file.seek(sequenz * 1300);
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
    public void runMiddleSequenzTest(){
        BCPPacket.addInt(BCPPacket.getSequenzPos(), middleSequenz, packet.getData());

        FileDataRecieveHandler handler = new FileDataRecieveHandler(packet);
        handler.run();

        assertEquals(0, DownloadFiles.getInstance().getFile(srcID, fileId).getNextNeededChunk());


        byte[] expectedPayload = new byte[payloadlenght];
        try(RandomAccessFile file = new RandomAccessFile(newFilePath, "r")){
            file.seek(sequenz * 1300);
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