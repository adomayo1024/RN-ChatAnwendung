package ChatAnwendung.Impl;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

public class MessageQueue {

    private  static MessageQueue INSTANCE;

    private final List<DatagramPacket> queue;

    private final Semaphore queueSemaphore;

    private final ReentrantLock queueMutex;

    private MessageQueue(){
        queue = new ArrayList<>();
        queueSemaphore = new Semaphore(0);
        queueMutex = new ReentrantLock(true);
    }


    public static MessageQueue getInstance() {
        if(INSTANCE == null){
            INSTANCE = new MessageQueue();
        }
        return INSTANCE;
    }

    public DatagramPacket poll() throws InterruptedException{
        queueSemaphore.acquire();
        queueMutex.lock();
        DatagramPacket packet = queue.removeFirst();
        queueMutex.unlock();
        return packet;
    }

    public void push(DatagramPacket packet) {
        queueMutex.lock();
        queue.add(packet);
        queueMutex.unlock();
        queueSemaphore.release();
    }

    public void pushAtFirst(DatagramPacket packet) {
        queueMutex.lock();
        queue.addFirst(packet);
        queueMutex.unlock();
        queueSemaphore.release();
    }
}
