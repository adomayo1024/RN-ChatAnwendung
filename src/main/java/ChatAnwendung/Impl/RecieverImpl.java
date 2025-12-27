package ChatAnwendung.Impl;


import ChatAnwendung.Api.Reciever;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.Handler.ReceiverHandlers.RecieverHandlerImpl;
import ChatAnwendung.Impl.persistence.Storage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

@Slf4j
public class RecieverImpl implements Runnable, Reciever {

    DatagramSocket socket;

    private RecieverHandlerImpl handler;

    private final int PACKETSIZE = 1400;

    public RecieverImpl(DatagramSocket socket, RecieverHandlerImpl handler) {
        this.socket = socket;
        this.handler = handler;
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

                handler.handle(request);


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
