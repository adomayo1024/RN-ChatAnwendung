package ChatAnwendung.persistence.Impl;

import ChatAnwendung.persistence.Api.ConnectionList;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ConnectionListImpl implements ConnectionList {

    // Liste aller connections
    private List<Connection> connections;

    // Mutex für den Zugriff
    private ReentrantLock mutex;

    public ConnectionListImpl(){
        connections = new ArrayList<>();
        mutex = new ReentrantLock();
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
