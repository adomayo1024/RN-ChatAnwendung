package ChatAnwendung.Impl;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class Storage {

    private static Storage INSTANCE;

    private final ExecutorService threadPool;

    private final List<DatagramPacket> sendPackageList;

    private final RoutingTableImpl routingtable;

    private final ReentrantLock sendPackageMutex;

    private ReentrantLock routingTableMutex;

    private Storage(){
        threadPool = Executors.newFixedThreadPool(10);
        sendPackageList = new ArrayList<>();
        routingtable = new RoutingTableImpl();
        sendPackageMutex = new ReentrantLock(true);
        routingTableMutex = new ReentrantLock(true);
    }

    public static Storage getInstance(){
        if(INSTANCE == null) {
            INSTANCE = new Storage();
        }

        return INSTANCE;
    }

    public void addSendPackage(DatagramPacket packet) {
        sendPackageMutex.lock();
        sendPackageList.add(packet);
        sendPackageMutex.unlock();
    }

    public DatagramPacket getNextSendPackage(){
        sendPackageMutex.lock();
        DatagramPacket packet = sendPackageList.removeFirst();
        sendPackageMutex.unlock();
        return packet;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    public void addRoutingEntry(RoutingEntryImpl entry) {
        routingTableMutex.lock();
        routingtable.add(entry);
        routingTableMutex.unlock();
    }
}
