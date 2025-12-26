package ChatAnwendung.Impl;

import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Level;

@Slf4j
public class RequestSender extends AbstractHandler {

    private File file;

    public RequestSender(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        int sequenz = file.getNextNeededChunk();
        if(sequenz == -1 || timeSinceLastFileDataPackageMoreThanASecond()){
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

        log.debug( "Sent Request for FileID " + fileID + " and sequence: " + sequenz + "from the User: " + Long.toUnsignedString(destUID));
    }

    private boolean timeSinceLastFileDataPackageMoreThanASecond(){
        return System.currentTimeMillis() - file.getRecievedLastChunk() > 1_000;
    }
}
