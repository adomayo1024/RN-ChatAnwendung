package ChatAnwendung.persistence.Impl;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Getter
@Slf4j
public class ThreadPools implements ChatAnwendung.persistence.Api.ThreadPools {

    // Executor Services für die RequestSender
    private final ScheduledExecutorService fileRequestTimer;

    // Executor Service für alle Services, die in einem Intervall passieren
    private final ScheduledExecutorService scheduleServices;

    // Executor Service für den Sender
    private final ExecutorService senderThreadPool;

    // Executor Service für den InputHandler
    private final ExecutorService inputHandlerThreadPool;

    // Executor Service für den Receiver
    private final ExecutorService receiverThreadPool;

    // Executor Service für den InputHandler und den ReceiveHandler
    private final ExecutorService workerThreadPool;

    // Future des ScheduleServices Threads
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

    @Override
    public void setScheduleServicesFuture(ScheduledFuture<?> scheduleServicesFuture) {
        this.scheduleServicesFuture = scheduleServicesFuture;
    }
}
