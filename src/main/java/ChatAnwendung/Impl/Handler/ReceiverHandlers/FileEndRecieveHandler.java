package ChatAnwendung.Impl.Handler.ReceiverHandlers;

import ChatAnwendung.Impl.persistence.DownloadFiles;
import ChatAnwendung.Impl.persistence.File;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;

@Slf4j
public class FileEndRecieveHandler extends AbstractRecieveHanlder{

    public FileEndRecieveHandler( DatagramPacket packet) {
        super(packet);
    }

    @Override
    public void run(){

        log.debug("Received File End");
    }
}
