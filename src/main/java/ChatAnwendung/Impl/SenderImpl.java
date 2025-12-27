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

            DatagramPacket p;
            try {

            log.debug("Start polling for new package");

            p = MessageQueue.getInstance().poll();

            log.debug("New Package for sending found");

            socket.send(p);

                log.debug("send a package to the address: {} and to port: {} of Type: {}", p.getAddress(), p.getPort(), PacketTypes.values()[p.getData()[1]]);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        log.debug( "Sender shutdown");
    }
}
