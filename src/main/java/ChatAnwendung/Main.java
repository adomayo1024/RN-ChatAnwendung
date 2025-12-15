package ChatAnwendung;

import ChatAnwendung.Impl.*;
import ChatAnwendung.Impl.Handler.InputHandlers.InputHandlerImpl;
import ChatAnwendung.Impl.Handler.RecieverHandlers.RecieverHandlerImpl;

import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {


    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        logger.log(Level.INFO, "Starting ChatAnwendung");

        try(DatagramSocket socket = new DatagramSocket(0)){

            Storage.getInstance().setPort(socket.getLocalPort());
            logger.log(Level.INFO, "Socket opened on port " + socket.getLocalPort());
            logger.log(Level.INFO, "You got the ID: " + Storage.getInstance().getUnsignedID());
            logger.log(Level.INFO, "Sender mode is " + Storage.getInstance().getSendMode());

            CompletableFuture<Void> inputHandler = CompletableFuture.runAsync(new InputReaderImpl(new InputHandlerImpl()), ThreadPools.getInstance().getThreadPool());
            CompletableFuture<Void> reciever = CompletableFuture.runAsync(new RecieverImpl(socket, new RecieverHandlerImpl()), ThreadPools.getInstance().getThreadPool());
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socket), ThreadPools.getInstance().getThreadPool());
            inputHandler.join();
            reciever.cancel(true);
            sender.cancel(true);

        } catch (SocketException e) {
            throw new RuntimeException();
        } finally{
            ThreadPools.getInstance().shutDown();
            logger.log(Level.INFO, "Anwendung beendet");
        }
    }

}