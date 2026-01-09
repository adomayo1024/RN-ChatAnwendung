package ChatAnwendung.persistence.Impl;

import ChatAnwendung.persistence.Api.RoutingEntry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class RoutingEntryImpl implements RoutingEntry {

    // Die NodeId des Host
    @Getter
    private long NodeId;

    // Die Adresse, zu der ein Paket gesendet werden soll, wenn es an die NodeId gehen soll
    @Getter
    private InetAddress nextHopAddress;

    // Der Port, an dem ein Paket gesendet werden soll, wenn es an die NodeId gehen soll
    @Getter
    private int nextHopPort;

    // Wie viel Hops der Host entfernt ist
    @Getter
    private byte hops;

    // Der Zeitpunkt, zu dem der Host zuletzt gesehen wurde
    @Getter
    private long lastSeen;

    // Ob der Host zurzeit erreichbar ist
    private boolean routable;

    // Mutex für das Setzten der Attribute
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
