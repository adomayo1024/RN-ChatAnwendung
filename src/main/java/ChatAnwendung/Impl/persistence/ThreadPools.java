package ChatAnwendung.Impl.persistence;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Slf4j
public class ThreadPools {

    private static ThreadPools INSTANCE;

    private static final ReentrantLock getMutex = new ReentrantLock(true);

    private final ExecutorService threadPool;

    private final ScheduledExecutorService fileRequestTimer;

    private final ScheduledExecutorService hearbteatAndRoutingTableTimer;

    private final ScheduledExecutorService timeoutTimer;

    private final ExecutorService senderThreadPool;

    private final ExecutorService inputHandlerThreadPool;

    private final ExecutorService receiverThreadPool;

    @Setter
    private ScheduledFuture<?> heartBeatAndRoutingTableTimerFuture;

    @Setter
    private ScheduledFuture<?> timeoutFuture;

    private ThreadPools(){
        threadPool = Executors.newFixedThreadPool(3);
        fileRequestTimer = Executors.newScheduledThreadPool(3);
        hearbteatAndRoutingTableTimer = Executors.newScheduledThreadPool(1);
        timeoutTimer = Executors.newScheduledThreadPool(1);
        senderThreadPool = Executors.newFixedThreadPool(1);
        inputHandlerThreadPool = Executors.newFixedThreadPool(1);
        receiverThreadPool = Executors.newFixedThreadPool(1);
    }


    public static ThreadPools getInstance(){
        getMutex.lock();
        if(INSTANCE == null){
            INSTANCE = new ThreadPools();
        }
        getMutex.unlock();
        return INSTANCE;
    }


    public void shutDown(){
        threadPool.shutdownNow();
        fileRequestTimer.shutdownNow();
        hearbteatAndRoutingTableTimer.shutdownNow();
        timeoutTimer.shutdownNow();
        senderThreadPool.shutdownNow();
        inputHandlerThreadPool.shutdownNow();
        receiverThreadPool.shutdownNow();
        if(timeoutFuture != null){
            timeoutFuture.cancel(true);
        }

        if(timeoutFuture != null){
            timeoutFuture.cancel(true);
        }

    }
}
