package ChatAnwendung.facade.impl;

import ChatAnwendung.facade.Api.Sender;
import ChatAnwendung.logic.Enums.PacketTypes;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class SenderImpl implements Sender, Runnable {

    private DatagramSocket socket;

    private final BlockingQueue<DatagramPacket> sendeQueue;

    public SenderImpl(DatagramSocket socket, BlockingQueue<DatagramPacket> sendeQueue) {
        this.socket = socket;
        this.sendeQueue = sendeQueue;
    }

    @Override
    public void run() {

        boolean interrupted = false;

        while (!interrupted) {

            DatagramPacket p;
            try {

            log.debug("Start polling for new package");

            p = sendeQueue.take();

            log.debug("New Package for sending found");



            socket.send(p);

                log.debug("send a package to the address: {} and to port: {} of Type: {}", p.getAddress(), p.getPort(), PacketTypes.values()[p.getData()[1]]);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            } catch (InterruptedException e){
                interrupted = true;
            }
        }

        log.debug( "Sender shutdown");
    }
}
