package ChatAnwendung.Impl;

import ChatAnwendung.Api.Reciever;

import java.net.DatagramSocket;

public class RecieverImpl implements Runnable, Reciever {

    DatagramSocket socket;

    public RecieverImpl(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {


    }
}
