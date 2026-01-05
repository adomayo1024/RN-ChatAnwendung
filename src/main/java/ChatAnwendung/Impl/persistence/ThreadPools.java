package ChatAnwendung.Impl.persistence;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Slf4j
public class ThreadPools {

    private final ScheduledExecutorService fileRequestTimer;

    private final ScheduledExecutorService hearbteatAndRoutingTableTimer;

    private final ScheduledExecutorService timeoutTimer;

    private final ExecutorService senderThreadPool;

    private final ExecutorService inputHandlerThreadPool;

    private final ExecutorService receiverThreadPool;

    private final ExecutorService workerThreadPool;

    @Setter
    private ScheduledFuture<?> heartBeatAndRoutingTableTimerFuture;

    @Setter
    private ScheduledFuture<?> timeoutFuture;

    public ThreadPools(){
        fileRequestTimer = Executors.newScheduledThreadPool(3);
        hearbteatAndRoutingTableTimer = Executors.newScheduledThreadPool(1);
        timeoutTimer = Executors.newScheduledThreadPool(1);
        senderThreadPool = Executors.newFixedThreadPool(1);
        inputHandlerThreadPool = Executors.newFixedThreadPool(1);
        receiverThreadPool = Executors.newFixedThreadPool(1);
        workerThreadPool = Executors.newFixedThreadPool(3);
    }


    public void shutDown(){
        fileRequestTimer.shutdownNow();
        hearbteatAndRoutingTableTimer.shutdownNow();
        timeoutTimer.shutdownNow();
        senderThreadPool.shutdownNow();
        inputHandlerThreadPool.shutdownNow();
        receiverThreadPool.shutdownNow();
        workerThreadPool.shutdownNow();
        if(timeoutFuture != null){
            timeoutFuture.cancel(true);
        }

        if(timeoutFuture != null){
            timeoutFuture.cancel(true);
        }

    }
}
