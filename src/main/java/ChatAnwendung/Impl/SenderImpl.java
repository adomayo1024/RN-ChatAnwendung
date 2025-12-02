package ChatAnwendung.Impl;

import ChatAnwendung.Api.Sender;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SenderImpl implements Sender, Runnable {

    private DatagramSocket socket;

    private final Logger logger;

    public SenderImpl(DatagramSocket socket) {

        this.socket = socket;
        logger = Logger.getLogger(SenderImpl.class.getName());
    }

    @Override
    public void run() {

        while (!Thread.interrupted()) {
            DatagramPacket p= null;
            try {
            logger.log(Level.INFO, "Packet holen");
            p = Storage.getInstance().getNextSendPackage();
            logger.log(Level.INFO, "Packet bekommen");
                socket.send(p);
            } catch (Exception e) {
                e.printStackTrace();
            }

            logger.log(Level.INFO, "send a package to the adress: " + p.getAddress() + " and to port: " + p.getPort());
        }

        logger.log(Level.INFO, "Sender shutdown");

    }
}
