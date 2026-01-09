package ChatAnwendung.persistence.Impl;

import java.net.InetAddress;

/**
 * Diese Klasse speichert eine Verbindung zu einem Host.
 * @param address die Addresse des Hosts
 * @param port der Port des Hosts
 */
public record Connection(InetAddress address, int port) {

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Connection(InetAddress address1, int port1))){
            return false;
        }

        return address.equals(address1) && port == port1;
    }

    @Override
    public int hashCode(){
        return address.hashCode() ^ port;
    }
}
