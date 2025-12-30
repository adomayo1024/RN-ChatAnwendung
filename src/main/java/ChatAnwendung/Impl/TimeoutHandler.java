package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;
import ChatAnwendung.Impl.persistence.RoutingTableImpl;
import lombok.extern.slf4j.Slf4j;

import static java.lang.Thread.sleep;

@Slf4j
public class TimeoutHandler extends AbstractHandler {

    private final int timeout = 45_000;
    public TimeoutHandler() {
    }

    @Override
    public void run() {

        int sleepTime = timeout;
        try {
            do{
                sleep(sleepTime);
                sleepTime = 0;
                for(RoutingEntry entry : RoutingTableImpl.getInstance().getAllEntries()){
                    int lastSeen = (int)(System.currentTimeMillis() - entry.getLastSeen());
                    if(lastSeen >= timeout){
                        RoutingTableImpl.getInstance().removeUID(entry.getUID());
                        log.debug("RoutingEntry removed for {} because of Timeout", Long.toUnsignedString(entry.getUID()));
                        System.out.println("RoutingEntry removed for " + Long.toUnsignedString(entry.getUID()) + " because of Timeout");
                    }
                    else {
                        if(lastSeen > sleepTime){
                            sleepTime = timeout - lastSeen;
                        }
                    }
                }

                if(sleepTime == 0){
                    sleepTime = timeout;
                }
            }while(!Thread.currentThread().isInterrupted());
        } catch (InterruptedException e) {
            log.debug( "TimeoutHandler was interrupted");
        }
    }
}
