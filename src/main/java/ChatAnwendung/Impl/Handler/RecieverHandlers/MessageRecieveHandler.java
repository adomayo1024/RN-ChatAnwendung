package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.Header;
import lombok.extern.slf4j.Slf4j;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class MessageRecieveHandler extends AbstractRecieveHanlder {
    public MessageRecieveHandler(DatagramPacket packet) {
        super(packet);
    }

    @Override
    public void run(){
        byte[] data = packet.getData();
        short payloadLength = getPayloadLength(data);
        StringBuilder terminalOutput = new StringBuilder();

        terminalOutput.append("You received a message from: ");
        terminalOutput.append(Long.toUnsignedString(getSrcUID(data)));
        terminalOutput.append(": ");

        if(payloadLength >= 1){
            byte[] message = new byte[payloadLength];
            System.arraycopy(data, Header.getPayloadPos(), message, 0, payloadLength);
            terminalOutput.append(new String(message, StandardCharsets.UTF_8));
        }

        System.out.println(terminalOutput);
    }
}
