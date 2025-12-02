package ChatAnwendung.Impl;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class Storage {

    private static Storage INSTANCE;

    private final ExecutorService threadPool;

    private final List<DatagramPacket> sendPackageList;

    private final RoutingTableImpl routingtable;

    private final ReentrantLock sendPackageMutex;

    private final Semaphore sendPackagesCountMutex;

    private final Long broadcastId;

    private long ID;

    private ReentrantLock routingTableMutex;
    private int port;

    private Storage(){
        threadPool = Executors.newFixedThreadPool(10);
        sendPackageList = new ArrayList<>();
        routingtable = new RoutingTableImpl();
        sendPackageMutex = new ReentrantLock(true);
        routingTableMutex = new ReentrantLock(true);
        sendPackagesCountMutex = new Semaphore(0);
        broadcastId = -1L;
    }

    public static Storage getInstance(){
        if(INSTANCE == null) {
            INSTANCE = new Storage();
        }

        return INSTANCE;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public long getID() {
        return ID;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort(){
        return port;
    }

    public void addSendPackage(DatagramPacket packet) {
        sendPackageMutex.lock();
        sendPackageList.add(packet);
        sendPackageMutex.unlock();
        sendPackagesCountMutex.release();
    }

    public DatagramPacket getNextSendPackage(){
        try {
            sendPackagesCountMutex.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sendPackageMutex.lock();
        DatagramPacket packet = sendPackageList.removeFirst();
        sendPackageMutex.unlock();
        return packet;
    }

    public boolean isUIDavailable(long uID) {
        return routingtable.isUIDavailable();
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void addRoutingEntry(RoutingEntryImpl entry) {
        routingTableMutex.lock();
        routingtable.add(entry);
        routingTableMutex.unlock();
    }

    private boolean hasSendPackage(DatagramPacket packet){
        return !sendPackageList.isEmpty();
    }

    public long getBroadCastId(){
        return broadcastId;
    }

    public InetAddress getnextHopAdressForUid(long uID) {
        return routingtable.getNextHopAdressForUID(uID);
    }

    public int getNextHopPortForUID(long uID) {
        return routingtable.getNextHopPortFroUID(uID);
    }

    public void shutDown(){
        threadPool.close();
    }
}
