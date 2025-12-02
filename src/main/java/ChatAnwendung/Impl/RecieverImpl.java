package ChatAnwendung.Impl;


import ChatAnwendung.Api.Reciever;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.CancellationException;
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

        try {
            DatagramPacket request = new DatagramPacket(new byte[1400], 1400);

            socket.receive(request);



        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CancellationException e) {
            logger.log(Level.INFO, "Reciever turned down");
        }

    }
}
