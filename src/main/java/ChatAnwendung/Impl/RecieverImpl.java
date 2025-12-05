package ChatAnwendung.Impl;


import ChatAnwendung.Api.Reciever;
import ChatAnwendung.Impl.Handler.RecieverHandlers.RecieverHandlerImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecieverImpl implements Runnable, Reciever {

    DatagramSocket socket;

    private final Logger logger;

    private final int PACKETSIZE = 1400;

    public RecieverImpl(DatagramSocket socket) {
        this.socket = socket;
        logger = Logger.getLogger(RecieverImpl.class.getName());
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

        try {
            DatagramPacket request = new DatagramPacket(new byte[1400], 1400);

            socket.receive(request);

            CompletableFuture.runAsync(new RecieverHandlerImpl(request));



        } catch (IOException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.INFO, "Reciever got Interrupted");
        }
            }

        logger.log(Level.INFO, "Reciever turned down");

    }
}
