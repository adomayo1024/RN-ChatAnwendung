package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;

import java.net.InetAddress;

public class RoutingEntryImpl implements RoutingEntry {

    private long UID;

    private InetAddress nextHopAdress;

    private int nextHopPort;

    private int hops;

    private long last_seen;

    private boolean routable;

    public RoutingEntryImpl(long UID, InetAddress adress, int nextHopPort, int hops, long last_seen) {
        this.UID = UID;
        this.nextHopAdress = adress;
        this.nextHopPort = nextHopPort;
        this.hops = hops;
        this.last_seen = last_seen;
        routable = true;
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
        nextHopPort = port;
    }

    @Override
    public InetAddress getNextHopAdress() {
        return nextHopAdress;
    }

    @Override
    public void setNextHopAdress(InetAddress adress){
        nextHopAdress = adress;
    }

    @Override
    public int getHops() {
        return hops;
    }

    @Override
    public void setHops(int hops){
        this.hops = hops;
    }

    @Override
    public boolean isRoutable(){
        return routable;
    }

    @Override
    public void setRoutable(boolean routable){
        this.routable = routable;
    }

    @Override
    public long getLast_seen(){
        return last_seen;
    }
}
