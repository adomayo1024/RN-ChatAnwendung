package ChatAnwendung;

import ChatAnwendung.persistence.Api.RoutingTable;
import ChatAnwendung.logic.Impl.InputHandler;
import ChatAnwendung.logic.Impl.ReceiveHanlder;
import ChatAnwendung.facade.impl.InputReaderImpl;
import ChatAnwendung.facade.impl.RecieverImpl;
import ChatAnwendung.facade.impl.SenderImpl;
import ChatAnwendung.persistence.Impl.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Main {

    public static void main(String[] args) throws SocketException {


        InetAddress address = null;

        boolean found = false;

        Enumeration<NetworkInterface> interfaces =
                NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements() && !found) {
            NetworkInterface ni = interfaces.nextElement();

            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                System.out.println("  Address: " + addr.getHostAddress());

                if(addr.getHostAddress().contains("172.18.27.129")){
                    address = addr;
                    found = true;
                    break;

                }
            }
            System.out.println();
        }


        log.info( "Starting ChatAnwendung");

        ThreadPools threadPools = new ThreadPools();
        ConnectionsList connectionsList = new ConnectionsList();
        DownloadFiles downloadFiles = new DownloadFiles(threadPools.getFileRequestTimer());
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

        int port = 5000;


        try(DatagramSocket socketRecieve = new DatagramSocket(port, address); DatagramSocket socketSend = new DatagramSocket()){

            storage.setPort(socketRecieve.getLocalPort());
            storage.setAddress(socketRecieve.getLocalAddress());
            log.info("Socket opened on port {}", socketRecieve.getLocalPort());
            log.info("You got the ID: {}", storage.getUnsignedID());

            System.out.println("Socket opened on port " + socketRecieve.getLocalPort());
            System.out.println("You got the ID: " + storage.getUnsignedID());

            CompletableFuture<Void> inputReader = CompletableFuture.runAsync(new InputReaderImpl(inputQueue, storage), threadPools.getInputHandlerThreadPool());
            CompletableFuture<Void> receiver = CompletableFuture.runAsync(new RecieverImpl(socketRecieve, receiveQueue), threadPools.getReceiverThreadPool());
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socketSend, sendeQueue), threadPools.getSenderThreadPool());

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