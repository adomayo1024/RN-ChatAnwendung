package ChatAnwendung.Impl;


import ChatAnwendung.Api.Reciever;
import ChatAnwendung.Impl.Handler.RecieverHandlers.RecieverHandlerImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecieverImpl implements Runnable, Reciever {

    DatagramSocket socket;

    private final Logger logger;

    private RecieverHandlerImpl handler;

    private final int PACKETSIZE = 1400;

    public RecieverImpl(DatagramSocket socket, RecieverHandlerImpl handler) {
        this.socket = socket;
        this.handler = handler;
        logger = Logger.getLogger(RecieverImpl.class.getName());
    }

    @Override
    public void run() {

        if(Storage.getInstance().getSendMode() == SendMode.SELF) {
            try {
                socket = new DatagramSocket(8080);
            } catch (SocketException e) {
                throw new RuntimeException(e);
            }
        }
        while (!Thread.currentThread().isInterrupted()) {


        try {
            DatagramPacket request = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);

            socket.receive(request);

            handler.handle(request);



        } catch (IOException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.INFO, "Reciever got Interrupted");
        }
            }

        logger.log(Level.INFO, "Reciever turned down");

    }
}
