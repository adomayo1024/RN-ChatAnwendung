package ChatAnwendung.Impl.FrequentlySender;

import ChatAnwendung.Impl.persistence.File;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.MessageQueue;
import ChatAnwendung.Impl.PacketTypes;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;

@Slf4j
public class RequestSender extends AbstractHandler {

    private File file;

    public RequestSender(File file) {
        this.file = file;
    }

    @Override
    public void run() {

        log.debug("Start with sending Request");

        int sequenz = file.getNextNeededChunk();
        if(sequenz == -1 || !timeSinceLastFileDataPackageMoreThanASecond()){
            return;
        }
        byte[] payload = new byte[0];
        long destUID = file.getSrcUID();
        InetAddress destAdress = RoutingTableImpl.getInstance().getNextHopAdressForUID(destUID);
        int destPort = RoutingTableImpl.getInstance().getNextHopPortForUID(destUID);
        int fileID = file.getFileId();
        DatagramPacket reqeuestPacket = makeDatagramPackage(
                PacketTypes.RESENDREQUEST,
                destUID,
                sequenz,
                fileID,
                payload,
                destAdress,
                destPort);
        MessageQueue.getInstance().push(reqeuestPacket);

        file.inkrementRequestCountWithoutResponse();

        log.debug("Sent Request for FileID {} and sequence: {}from the User: {}", fileID, sequenz, Long.toUnsignedString(destUID));
    }

    private boolean timeSinceLastFileDataPackageMoreThanASecond(){
        return System.currentTimeMillis() - file.getRecievedLastChunk() > 1_000;
    }
}
