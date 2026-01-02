package Handlers.ReceiveHandlers;

import ChatAnwendung.Impl.FrequentlySender.RequestSender;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.AbstractRecieveHanlder;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.RequestRecieveHandler;
import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.File;
import ChatAnwendung.Impl.persistence.Storage;
import ChatAnwendung.Impl.persistence.ThreadPools;
import org.junit.jupiter.api.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class RequestReceiveHanlderTest {
    private static String filePath = "C:\\Users\\leons\\Desktop\\requestSenderTest.txt";

    private static int zeile = 0;

    private static Long srcId = 123L;

    private static Integer fileId = 12;

    private static int sequenz = 1;

    private static InetAddress address = InetAddress.getLoopbackAddress();

    private static int port = 5000;

    private static DatagramPacket packet;

    private static String fileContent = "" +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++ +
            zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ + zeile++ +zeile++ + zeile++;


    @BeforeEach
    public void createFileAndPacket(){

        // Shutdowns all Thread so nobody can put something from the MessageQueue
        ThreadPools.getInstance().shutDown();


        Storage.getInstance().setSendOpenFile(fileId, filePath);

        Path path = Paths.get(filePath);

        try {
            Files.writeString(
                    path,
                    fileContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

    @AfterEach
    public void deleteFile(){
        try {
            Files.delete(Paths.get(filePath));
        } catch(FileNotFoundException | DirectoryNotEmptyException e){

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterAll
    public static void shutDown(){
        ThreadPools.getInstance().shutDown();
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
            file.seek(1300);
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
    public void runWithLastSequenzTest(){

        int sequenz = 4;
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
            expectedPayload = new byte[(int)file.length() - (4 * 1300)];
            file.seek(4 * 1300);
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