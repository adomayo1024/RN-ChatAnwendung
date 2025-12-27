package ChatAnwendung.Impl.persistence;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ConnectionsList {

    private static ConnectionsList INSTANCE;

    private static final ReentrantLock getMutex = new ReentrantLock(true);

    private List<Connection> connections;

    private ReentrantLock mutex;

    private ConnectionsList(){
        connections = new ArrayList<>();
        mutex = new ReentrantLock();
    }

    public static ConnectionsList getInstance(){
        getMutex.lock();
        if(INSTANCE == null){
            INSTANCE = new ConnectionsList();
            log.debug("Created new Connection List: {}" , INSTANCE);
        }
        getMutex.unlock();
        return INSTANCE;
    }


    public void add(Connection connection){
        mutex.lock();
        connections.add(connection);
        mutex.unlock();

        log.debug("Added new Connection to List: {}", this);
    }

    public void remove(Connection connection){
        mutex.lock();
        connections.remove(connection);
        mutex.unlock();

        log.debug("Removed Connection from List: {}", this);
    }

    public List<Connection> getAllConnections(){
        mutex.lock();
        List<Connection> result = List.copyOf(connections);
        mutex.unlock();

        log.debug("Returned all Connections from List: {}", this);
        return result;
    }


}
