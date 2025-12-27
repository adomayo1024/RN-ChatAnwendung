package ChatAnwendung.Impl;

import ChatAnwendung.Api.Sender;
import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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

            log.debug( "Packet holen");

            p = MessageQueue.getInstance().poll();

            log.debug( "Packet bekommen");

            if(Storage.getInstance().getSendMode() != SendMode.NOTHING){
                socket.send(p);
            }
            log.debug( "send a package to the adress: " + p.getAddress() + " and to port: " + p.getPort() + " of Type: " + SendMode.values()[p.getData()[1]]);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        log.debug( "Sender shutdown");
    }
}
