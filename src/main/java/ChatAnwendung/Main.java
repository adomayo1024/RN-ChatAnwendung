package ChatAnwendung;

import ChatAnwendung.logic.Api.InputHandler;
import ChatAnwendung.logic.Api.ReceiveHandler;
import ChatAnwendung.persistence.Api.ConnectionList;
import ChatAnwendung.persistence.Api.DownloadFiles;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.logic.Impl.InputHandlerImpl;
import ChatAnwendung.logic.Impl.ReceiveHandlerImpl;
import ChatAnwendung.facade.impl.InputReaderImpl;
import ChatAnwendung.facade.impl.ReceiverImpl;
import ChatAnwendung.facade.impl.SenderImpl;
import ChatAnwendung.persistence.Api.Storage;
import ChatAnwendung.persistence.Impl.*;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;

@Slf4j
public class Main {

    public static void main(String[] args)  {

        log.info( "Starting ChatAnwendung");

        ThreadPools threadPools = new ThreadPools();
        ConnectionList connectionsList = new ConnectionListImpl();
        DownloadFiles downloadFiles = new DownloadFilesImpl(threadPools.getFileRequestTimer());
        RoutingTable routingTable = new RoutingTableImpl();
        Storage storage;
        try {
            storage = new StorageImpl();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        BlockingQueue<DatagramPacket> receiveQueue = new ArrayBlockingQueue<>(10000);
        BlockingDeque<DatagramPacket> sendeQueue = new LinkedBlockingDeque<>(10000);
        BlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(50);

        InputHandler inputHandler = new InputHandlerImpl(inputQueue, routingTable, connectionsList, storage, sendeQueue, threadPools);
        ReceiveHandler receiveHandler = new ReceiveHandlerImpl(receiveQueue, sendeQueue, routingTable, storage, downloadFiles);


        int port = args.length > 0 ?Integer.parseInt(args[0]) : 0;


        try(DatagramSocket socket = new DatagramSocket(port)){

            storage.setPort(socket.getLocalPort());
            log.info("Socket opened on port {}", socket.getLocalPort());
            log.info("You got the ID: {}", storage.getUnsignedID());

            System.out.println("Socket opened on port " + socket.getLocalPort());
            System.out.println("You got the ID: " + storage.getUnsignedID());

            CompletableFuture<Void> inputReader = CompletableFuture.runAsync(new InputReaderImpl(inputQueue, storage), threadPools.getInputHandlerThreadPool());
            CompletableFuture<Void> receiver = CompletableFuture.runAsync(new ReceiverImpl(socket, receiveQueue), threadPools.getReceiverThreadPool());
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socket, sendeQueue), threadPools.getSenderThreadPool());

            CompletableFuture<Void> inputHandlerFuture = CompletableFuture.runAsync(inputHandler, threadPools.getWorkerThreadPool());
            CompletableFuture<Void> receiveHandlerFuture1 = CompletableFuture.runAsync(receiveHandler, threadPools.getWorkerThreadPool());
            CompletableFuture<Void> receiveHandlerFuture2 = CompletableFuture.runAsync(receiveHandler, threadPools.getWorkerThreadPool());

            inputReader.join();

        } catch (SocketException e) {
            throw new RuntimeException();
        } finally{
            threadPools.shutDown();
            log.debug( "Anwendung beendet");
            System.out.println("Anwendung beendet");
        }
    }

}