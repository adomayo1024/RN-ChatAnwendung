package ChatAnwendung.Impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionsList {

    private static ConnectionsList INSTANCE;

    private List<Connection> connections;

    private ReentrantLock mutex;

    private ConnectionsList(){
        connections = new ArrayList<>();
        if(Storage.getInstance().getSendMode() != SendMode.ALL){
            try {
                connections.add(new Connection(InetAddress.getByName("127.0.0.1"), 8080));
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        mutex = new ReentrantLock();
    }

    public static ConnectionsList getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ConnectionsList();
        }

        return INSTANCE;
    }


    public void add(Connection connection){
        mutex.lock();
        connections.add(connection);
        mutex.unlock();
    }

    public void remove(Connection connection){
        mutex.lock();
        connections.remove(connection);
        mutex.unlock();
    }

    public List<Connection> getAllConnections(){
        mutex.lock();
        List<Connection> result = List.copyOf(connections);
        mutex.unlock();
        return result;
    }


}
