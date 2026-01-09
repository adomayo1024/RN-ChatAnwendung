package ChatAnwendung.persistence.Api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

public interface ThreadPools {

    /**
     * Beendet alle Threadpools, wenn noch ein Thread läuft wird dieser auch beendet.
     */
    void shutDown();

    //-------------- GETTER ---------------------

    ScheduledExecutorService getFileRequestTimer();

    ScheduledExecutorService getScheduleServices();

    ExecutorService getSenderThreadPool();

    ExecutorService getInputHandlerThreadPool();

    ExecutorService getReceiverThreadPool();

    ExecutorService getWorkerThreadPool();

    void setScheduleServicesFuture(ScheduledFuture<?> scheduleServicesFuture);
}
