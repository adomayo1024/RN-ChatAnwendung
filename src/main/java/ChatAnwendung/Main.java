package ChatAnwendung;

import ChatAnwendung.Api.RoutingTable;
import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.Handler.InputHandlers.InputHandler;
import ChatAnwendung.Impl.Handler.InputHandlers.InputHandlerImpl;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.ReceiveHanlder;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.RecieverHandlerImpl;
import ChatAnwendung.Impl.persistence.*;
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
        ConnectionsList connectionsList = new ConnectionsList();
        DownloadFiles downloadFiles = new DownloadFiles(threadPools.getTimeoutTimer());
        RoutingTable routingTable = new RoutingTableImpl();
        Storage storage;
        try {
            storage = new Storage();
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
            log.info("Socket opened on Address: {} and port {}", socket.getLocalAddress(), socket.getLocalPort());
            log.info("You got the ID: {}", storage.getUnsignedID());

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