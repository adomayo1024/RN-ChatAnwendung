package ChatAnwendung.Impl;

import java.net.InetAddress;

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
