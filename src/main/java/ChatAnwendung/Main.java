package ChatAnwendung;

import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.Handler.InputHandlers.InputHandlerImpl;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.RecieverHandlerImpl;
import ChatAnwendung.Impl.persistence.Storage;
import ChatAnwendung.Impl.persistence.ThreadPools;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class Main {

    public static void main(String[] args) {

        log.debug( "Starting ChatAnwendung");

        try(DatagramSocket socket = new DatagramSocket(0)){

            Storage.getInstance().setPort(socket.getLocalPort());
            log.debug("Socket opened on Address: {} and port {}", socket.getLocalAddress(), socket.getLocalPort());
            log.debug("You got the ID: {}", Storage.getInstance().getUnsignedID());

            CompletableFuture<Void> inputHandler = CompletableFuture.runAsync(new InputReaderImpl(new InputHandlerImpl()), ThreadPools.getInstance().getThreadPool());
            CompletableFuture<Void> reciever = CompletableFuture.runAsync(new RecieverImpl(socket, new RecieverHandlerImpl()), ThreadPools.getInstance().getThreadPool());
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socket), ThreadPools.getInstance().getThreadPool());
            inputHandler.join();

        } catch (SocketException e) {
            throw new RuntimeException();
        } finally{
            ThreadPools.getInstance().shutDown();
            log.debug( "Anwendung beendet");
            System.out.println("Anwendung beendet");
        }
    }

}