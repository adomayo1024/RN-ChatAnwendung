package ChatAnwendung.persistence.Impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Getter
@Slf4j
public class ThreadPools {

    private final ScheduledExecutorService fileRequestTimer;

    private final ScheduledExecutorService scheduleServices;

    private final ExecutorService senderThreadPool;

    private final ExecutorService inputHandlerThreadPool;

    private final ExecutorService receiverThreadPool;

    private final ExecutorService workerThreadPool;

    @Setter
    private ScheduledFuture<?> scheduleServicesFuture;

    public ThreadPools(){
        fileRequestTimer = Executors.newScheduledThreadPool(3);
        scheduleServices = Executors.newScheduledThreadPool(1);
        senderThreadPool = Executors.newFixedThreadPool(1);
        inputHandlerThreadPool = Executors.newFixedThreadPool(1);
        receiverThreadPool = Executors.newFixedThreadPool(1);
        workerThreadPool = Executors.newFixedThreadPool(3);
    }


    public void shutDown(){
        fileRequestTimer.shutdownNow();
        scheduleServices.shutdownNow();
        senderThreadPool.shutdownNow();
        inputHandlerThreadPool.shutdownNow();
        receiverThreadPool.shutdownNow();
        workerThreadPool.shutdownNow();
        if(scheduleServicesFuture != null){
            scheduleServicesFuture.cancel(true);
        }

    }
}
