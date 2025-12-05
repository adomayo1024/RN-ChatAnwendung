package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.Handler.AbstractHandler;
import ChatAnwendung.Impl.Handler.Header;
import ChatAnwendung.Impl.PacketTypes;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.logging.Logger;

public abstract class AbstractInputHandler extends AbstractHandler {

    protected String[] command;


    public AbstractInputHandler(String[] command, String name){
        super(name);
        this.command = command;
    }
}
