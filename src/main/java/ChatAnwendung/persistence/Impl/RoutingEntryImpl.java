package ChatAnwendung.persistence.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RoutingEntryImpl implements RoutingEntry {

    private long UID;

    private InetAddress nextHopAdress;

    private int nextHopPort;

    private byte hops;

    private long lastSeen;

    private boolean routable;
    private final ReentrantLock mutex;

    public RoutingEntryImpl(long UID, InetAddress address, int nextHopPort, byte hops, long lastSeen) {
        this(UID, address, nextHopPort, hops, lastSeen, true);
    }

    public RoutingEntryImpl(long UID, InetAddress adress, int nextHopPort, byte hops, long lastSeen, boolean routable){
        this.UID = UID;
        this.nextHopAdress = adress;
        this.nextHopPort = nextHopPort;
        this.hops = hops;
        this.lastSeen = lastSeen;
        this.routable = routable;
        this.mutex = new ReentrantLock();
    }


    @Override
    public Long getUID() {
        return UID;
    }

    @Override
    public int getNextHopPort() {
        return nextHopPort;
    }

    @Override
    public void setNextHopPort(int port){
        mutex.lock();
        nextHopPort = port;
        mutex.unlock();
    }

    @Override
    public InetAddress getNextHopAdress() {
        return nextHopAdress;
    }

    @Override
    public void setNextHopAdress(InetAddress adress){
        mutex.lock();
        nextHopAdress = adress;
        mutex.unlock();
    }

    @Override
    public byte getHops() {
        return hops;
    }

    @Override
    public void setHops(byte hops){
        mutex.lock();
        this.hops = hops;
        mutex.unlock();
    }

    @Override
    public boolean isRoutable(){
        return routable;
    }

    @Override
    public void setRoutable(boolean routable){
        mutex.lock();
        this.routable = routable;
        mutex.unlock();
    }

    @Override
    public long getLastSeen(){
        return lastSeen;
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
