package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;

import java.net.InetAddress;

public class RoutingEntryImpl implements RoutingEntry {

    private long UID;

    private InetAddress adress;

    private int port;

    private int hops;

    private long last_seen;

    public RoutingEntryImpl(long UID, InetAddress adress, int port, int hops, long last_seen) {
        this.UID = UID;
        this.adress = adress;
        this.port = port;
        this.hops = hops;
        this.last_seen = last_seen;
    }


    @Override
    public Long getUID() {
        return UID;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public InetAddress getAdress() {
        return adress;
    }

    @Override
    public int getHops() {
        return hops;
    }

    @Override
    public long getLast_seen(){
        return last_seen;
    }
}
