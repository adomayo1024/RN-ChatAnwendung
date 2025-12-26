package ChatAnwendung.Impl;

import ChatAnwendung.Api.Sender;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

@Slf4j
public class SenderImpl implements Sender, Runnable {

    private DatagramSocket socket;

    public SenderImpl(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            DatagramPacket p= null;
            try {

            log.info( "Packet holen");

            p = MessageQueue.getInstance().poll();

            log.info( "Packet bekommen");

            if(Storage.getInstance().getSendMode() != SendMode.NOTHING){
                socket.send(p);
            }
            log.info( "send a package to the adress: " + p.getAddress() + " and to port: " + p.getPort() + " of Type: " + SendMode.values()[p.getData()[1]]);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        log.info( "Sender shutdown");
    }
}
