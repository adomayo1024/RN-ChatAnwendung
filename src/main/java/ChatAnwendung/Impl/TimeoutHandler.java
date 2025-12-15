package ChatAnwendung.Impl;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Handler.Common.AbstractHandler;

import java.util.logging.Level;

import static java.lang.Thread.sleep;


public class TimeoutHandler extends AbstractHandler {

    private final int timeout = 45_000;
    public TimeoutHandler() {
        super(TimeoutHandler.class.getName());
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
            logger.log(Level.SEVERE, "TimeoutHandler was interrupted");
        }
    }
}
