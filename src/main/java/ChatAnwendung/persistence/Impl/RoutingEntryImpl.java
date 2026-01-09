package ChatAnwendung.persistence.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class RoutingEntryImpl implements RoutingEntry {

    @Getter
    private long NodeId;

    @Getter
    private InetAddress nextHopAddress;

    @Getter
    private int nextHopPort;

    @Getter
    private byte hops;

    @Getter
    private long lastSeen;

    private boolean routable;

    private final ReentrantLock mutex;

    /**
     *
     * @param NodeId
     * @param address
     * @param nextHopPort
     * @param hops
     * @param lastSeen
     */
    public RoutingEntryImpl(long NodeId, InetAddress address, int nextHopPort, byte hops, long lastSeen) {
        this(NodeId, address, nextHopPort, hops, lastSeen, true);
    }

    /**
     *
     * @param NodeId
     * @param adress
     * @param nextHopPort
     * @param hops
     * @param lastSeen
     * @param routable
     */
    public RoutingEntryImpl(long NodeId, InetAddress adress, int nextHopPort, byte hops, long lastSeen, boolean routable){
        this.NodeId = NodeId;
        this.nextHopAddress = adress;
        this.nextHopPort = nextHopPort;
        this.hops = hops;
        this.lastSeen = lastSeen;
        this.routable = routable;
        this.mutex = new ReentrantLock();
    }

    @Override
    public void setNextHopPort(int port){
        mutex.lock();
        nextHopPort = port;
        mutex.unlock();
    }

    @Override
    public void setNextHopAddress(InetAddress adress){
        mutex.lock();
        nextHopAddress = adress;
        mutex.unlock();
    }

    @Override
    public void setHops(byte hops){
        mutex.lock();
        this.hops = hops;
        mutex.unlock();
    }

    @Override
    public boolean getRoutable(){
        return routable;
    }

    @Override
    public void setRoutable(boolean routable){
        mutex.lock();
        this.routable = routable;
        mutex.unlock();
    }

    @Override
    public long getLastSeenShort(){
        return System.currentTimeMillis() - lastSeen;
    }

    @Override
    public void setLastSeen() {
        mutex.lock();
        lastSeen = System.currentTimeMillis();
        mutex.unlock();
    }
}
