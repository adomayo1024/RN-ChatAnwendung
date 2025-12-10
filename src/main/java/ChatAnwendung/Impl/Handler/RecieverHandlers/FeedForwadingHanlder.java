package ChatAnwendung.Impl.Handler.RecieverHandlers;

import ChatAnwendung.Impl.Header;
import ChatAnwendung.Impl.HearbeatSender;
import ChatAnwendung.Impl.MessageQueue;

import java.net.DatagramPacket;

public class FeedForwadingHanlder extends AbstractRecieveHanlder{
    public FeedForwadingHanlder(DatagramPacket packet) {
        super(FeedForwadingHanlder.class.getName(), packet);
    }

    @Override
    public void run(){
        byte[] data = packet.getData();
        byte ttl = getTtl(data);
        ttl -= 1;

        if(ttl > 0){
            byte hops = getHops(data);
            hops += 1;
            data[Header.getTtlPos()] = ttl;
            data[Header.getHopsPos()] = hops;
            MessageQueue.getInstance().push(packet);
        }
    }
}
