package ChatAnwendung.facade.impl;


import ChatAnwendung.facade.Api.Receiver;
import ChatAnwendung.Exceptions.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class ReceiverImpl implements Receiver {

    private final DatagramSocket socket;

    private final int PACKET_SIZE = 1400;

    private final BlockingQueue<DatagramPacket> queue;

    public ReceiverImpl(DatagramSocket socket, BlockingQueue<DatagramPacket> queue) {
        this.socket = socket;
        this.queue = queue;

    }

    @Override
    public void run() {

        boolean interrupted = false;

        while (!interrupted) {


            try {
                DatagramPacket request = new DatagramPacket(new byte[PACKET_SIZE], PACKET_SIZE);

                log.debug( "Waiting for new package");

                socket.receive(request);

                log.debug("new package received");

                queue.add(request);


            }catch (SocketException e) {
                interrupted = true;
                log.debug( "Socket closed");
            } catch (IllegalArgumentException e){
                ExceptionHandler.handle(e, this.getClass());
            }
            catch (IOException e) {
                interrupted = true;
                ExceptionHandler.handle(e, this.getClass());
            } catch (Exception e) {
                ExceptionHandler.handle(e, this.getClass());
            }
        }

        log.debug( "Receiver turned down");

    }
}
