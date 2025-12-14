package ChatAnwendung.Impl;

import java.util.concurrent.*;

public class ThreadPools {

    private static ThreadPools INSTANCE;

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
        if(INSTANCE == null){
            INSTANCE = new ThreadPools();
        }

        return INSTANCE;
    }


    public ExecutorService getThreadPool() {
        return threadPool;
    }


    public void shutDown(){
        threadPool.shutdown();
        fileRequestTimer.shutdownNow();
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
