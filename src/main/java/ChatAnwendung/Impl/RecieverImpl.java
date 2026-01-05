package ChatAnwendung.Impl;


import ChatAnwendung.Api.Reciever;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.RecieverHandlerImpl;
import ChatAnwendung.Impl.persistence.Storage;
import ChatAnwendung.Impl.persistence.ThreadPools;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RecieverImpl implements Runnable, Reciever {

    private final DatagramSocket socket;

    private final int PACKETSIZE = 1400;

    private final BlockingQueue<DatagramPacket> queue;

    public RecieverImpl(DatagramSocket socket, BlockingQueue<DatagramPacket> queue) {
        this.socket = socket;
        this.queue = queue;

    }

    @Override
    public void run() {

        boolean interrupted = false;

        while (!interrupted) {


            try {
                DatagramPacket request = new DatagramPacket(new byte[PACKETSIZE], PACKETSIZE);

                log.debug( "Waiting for new package");

                socket.receive(request);

                log.debug("new package received");

                queue.add(request);


            }catch (SocketException e) {
                interrupted = true;
                log.debug( "Socket closed");
            }
            catch (IOException e) {
                interrupted = true;
                ExceptionHandler.handle(e, this.getClass());
            }
        }

        log.debug( "Receiver turned down");

    }
}
