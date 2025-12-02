package ChatAnwendung;

import ChatAnwendung.Impl.Handler.InputHandlerImple;
import ChatAnwendung.Impl.InputReaderImpl;
import ChatAnwendung.Impl.RecieverImpl;
import ChatAnwendung.Impl.SenderImpl;
import ChatAnwendung.Impl.Storage;

import java.net.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {


    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        logger.log(Level.INFO, "Starting ChatAnwendung");

        try(DatagramSocket socket = new DatagramSocket(0)){

            socket.setBroadcast(true);
            logger.log(Level.INFO, "Socket opened on port " + socket.getLocalPort());

            CompletableFuture<Void> inputHandler = CompletableFuture.runAsync(new InputReaderImpl(new InputHandlerImple()))
                    .whenComplete((res, ex) ->{
                        if(ex != null) {
                            logger.log(Level.SEVERE, "InputHandler crashed/terminated with error", ex);
                        } else {
                            logger.log(Level.INFO, "InputHandler terminated normally");
                        }
                    });
            CompletableFuture<Void> reciever = CompletableFuture.runAsync(new RecieverImpl(socket));
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socket));
            inputHandler.join();
            reciever.cancel(true);
            sender.cancel(true);
            Storage.getInstance().shutDown();



            logger.log(Level.INFO, "Anwendung beendet");

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }




}