package ChatAnwendung;

import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.Handler.InputHandlers.InputHandlerImpl;
import ChatAnwendung.Impl.Handler.RecieverHandlers.RecieverHandlerImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class Main {

    public static void main(String[] args) {

        log.info( "Starting ChatAnwendung");

        try(DatagramSocket socket = new DatagramSocket(0)){

            Storage.getInstance().setPort(socket.getLocalPort());
            log.info( "Socket opened on Address: " + socket.getLocalAddress() + " and port " + socket.getLocalPort());
            log.info( "You got the ID: " + Storage.getInstance().getUnsignedID());
            log.info( "Sender mode is " + Storage.getInstance().getSendMode());

            CompletableFuture<Void> inputHandler = CompletableFuture.runAsync(new InputReaderImpl(new InputHandlerImpl()), ThreadPools.getInstance().getThreadPool());
            CompletableFuture<Void> reciever = CompletableFuture.runAsync(new RecieverImpl(socket, new RecieverHandlerImpl()), ThreadPools.getInstance().getThreadPool());
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socket), ThreadPools.getInstance().getThreadPool());
            inputHandler.join();

        } catch (SocketException e) {
            throw new RuntimeException();
        } finally{
            ThreadPools.getInstance().shutDown();
            log.info( "Anwendung beendet");
        }
    }

}