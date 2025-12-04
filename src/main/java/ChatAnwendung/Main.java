package ChatAnwendung;

import ChatAnwendung.Impl.Handler.InputHandlerImple;
import ChatAnwendung.Impl.InputReaderImpl;
import ChatAnwendung.Impl.RecieverImpl;
import ChatAnwendung.Impl.SenderImpl;
import ChatAnwendung.Impl.Storage;

import java.net.*;
import java.util.concurrent.CancellationException;
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

            CompletableFuture<Void> inputHandler = CompletableFuture.runAsync(new InputReaderImpl(new InputHandlerImple()), Storage.getInstance().getThreadPool());
            CompletableFuture<Void> reciever = CompletableFuture.runAsync(new RecieverImpl(socket), Storage.getInstance().getThreadPool());
            CompletableFuture<Void> sender = CompletableFuture.runAsync(new SenderImpl(socket), Storage.getInstance().getThreadPool());
            inputHandler.join();
            reciever.cancel(true);
            sender.cancel(true);

        } catch (SocketException e) {
            throw new RuntimeException();
        }catch (CancellationException e){

        } finally{
            Storage.getInstance().shutDown();
            logger.log(Level.INFO, "Anwendung beendet");
        }
    }

}