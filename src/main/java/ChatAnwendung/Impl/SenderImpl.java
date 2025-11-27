package ChatAnwendung.Impl;

import ChatAnwendung.Api.Sender;

import javax.xml.crypto.Data;
import java.net.DatagramSocket;

public class SenderImpl implements Sender, Runnable {

    DatagramSocket socket;

    public SenderImpl(DatagramSocket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

    }
}
