package ChatAnwendung;

import ChatAnwendung.persistence.Api.ConnectionList;
import ChatAnwendung.persistence.Api.DownloadFiles;
import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.logic.Impl.InputHandler;
import ChatAnwendung.logic.Impl.ReceiveHanlder;
import ChatAnwendung.facade.impl.InputReaderImpl;
import ChatAnwendung.facade.impl.RecieverImpl;
import ChatAnwendung.facade.impl.SenderImpl;
import ChatAnwendung.persistence.Api.Storage;
import ChatAnwendung.persistence.Impl.*;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

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
        BlockingQueue<DatagramPacket> sendeQueue = new ArrayBlockingQueue<>(10000);
        BlockingQueue<String> inputQueue = new ArrayBlockingQueue<>(50);

        InputHandler inputHandler = new InputHandler(inputQueue, routingTable, connectionsList, storage, sendeQueue, threadPools);
        ReceiveHanlder receiveHanlder = new ReceiveHanlder(receiveQueue, sendeQueue, routingTable, storage, downloadFiles);




        try(DatagramSocket socket = new DatagramSocket(0)){

            storage.setPort(socket.getLocalPort());
            log.info("Socket opened on port {}", socket.getLocalPort());
            log.info("You got the ID: {}", storage.getUnsignedID());

            System.out.println("Socket opened on port " + socket.getLocalPort());
            System.out.println("You got the ID: " + storage.getUnsignedID());

            CompletableFuture<Void> inputReader = CompletableFuture.runAsync(new InputReaderImpl(inputQueue, storage), threadPools.getInputHandlerThreadPool());
            CompletableFuture<Void> receiver = CompletableFuture.runAsync(new RecieverImpl(socket, receiveQueue), threadPools.getReceiverThreadPool());
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socket, sendeQueue), threadPools.getSenderThreadPool());

            CompletableFuture<Void> inputHandlerFuture = CompletableFuture.runAsync(inputHandler, threadPools.getWorkerThreadPool());
            CompletableFuture<Void> receiveHandlerFuture1 = CompletableFuture.runAsync(receiveHanlder, threadPools.getWorkerThreadPool());
            CompletableFuture<Void> receiveHandlerFuture2 = CompletableFuture.runAsync(receiveHanlder, threadPools.getWorkerThreadPool());

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