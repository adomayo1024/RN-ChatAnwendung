package ChatAnwendung.Impl.persistence;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ThreadPools {

    private static ThreadPools INSTANCE;

    private static final ReentrantLock getMutex = new ReentrantLock(true);

    private final ExecutorService threadPool;

    private final ScheduledExecutorService fileRequestTimer;

    private final ScheduledExecutorService heartBeatTimer;

    private ScheduledFuture<?> heartBeatTimerFuture;

    private CompletableFuture<Void> timeoutFuture;

    private ThreadPools(){
        threadPool = Executors.newFixedThreadPool(10);
        fileRequestTimer = Executors.newScheduledThreadPool(3);
        heartBeatTimer = Executors.newScheduledThreadPool(1);
    }


    public static ThreadPools getInstance(){
        getMutex.lock();
        if(INSTANCE == null){
            INSTANCE = new ThreadPools();
        }
        getMutex.unlock();
        return INSTANCE;
    }


    public ExecutorService getThreadPool() {
        return threadPool;
    }


    public void shutDown(){
        threadPool.shutdownNow();
        fileRequestTimer.shutdownNow();
        heartBeatTimer.shutdownNow();
        if(timeoutFuture != null){
            timeoutFuture.cancel(true);
        }

    }

    public  ScheduledExecutorService getFileRequestTimer() {
        return fileRequestTimer;
    }

    public ScheduledExecutorService getHeartBeatTimer() {
        return heartBeatTimer;
    }

    public ScheduledFuture<?> getHeartBeatTimerFuture() {
        return heartBeatTimerFuture;
    }

    public void setHeartBeatTimerFuture(ScheduledFuture<?> heartBeatTimerFuture) {
        this.heartBeatTimerFuture = heartBeatTimerFuture;
    }

    public CompletableFuture<Void> getTimeoutFuture(){
        return timeoutFuture;
    }

    public void setTimeoutFuture(CompletableFuture<Void> timeoutFuture){
        this.timeoutFuture = timeoutFuture;
    }
}
