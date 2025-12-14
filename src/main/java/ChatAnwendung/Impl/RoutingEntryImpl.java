package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;

import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;

public class RoutingEntryImpl implements RoutingEntry {

    private long UID;

    private InetAddress nextHopAdress;

    private int nextHopPort;

    private int hops;

    private long lastSeen;

    private boolean routable;
    private final ReentrantLock mutex;

    public RoutingEntryImpl(long UID, InetAddress adress, int nextHopPort, int hops, long lastSeen) {
        this.UID = UID;
        this.nextHopAdress = adress;
        this.nextHopPort = nextHopPort;
        this.hops = hops;
        this.lastSeen = lastSeen;
        routable = true;
        mutex = new ReentrantLock();
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
    public int getHops() {
        return hops;
    }

    @Override
    public void setHops(int hops){
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
    public void setLastSeen() {
        mutex.lock();
        lastSeen = System.currentTimeMillis();
        mutex.unlock();
    }
}
