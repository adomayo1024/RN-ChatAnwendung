package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Api.RoutingEntry;
import ChatAnwendung.Impl.Connection;
import ChatAnwendung.Impl.ConnectionsList;
import ChatAnwendung.Impl.Exceptions.ArgumentException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import ChatAnwendung.Impl.RoutingEntryImpl;
import ChatAnwendung.Impl.RoutingTableImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConnectHandler extends AbstractInputHandler {
    public ConnectHandler(String[] split) {
        super(split, ConnectHandler.class.getName());
    }

    @Override
    public void run(){

        InetAddress address;
        int port;

        try {
            address = InetAddress.getByName(command[1]);
            port = Integer.parseInt(command[2]);
            Connection connection = new Connection(address, port);

            ConnectionsList.getInstance().add(connection);
        } catch (UnknownHostException e) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }
    }

    public static String help() {
        return "connect: Verbindet diesen User direkt mit einen anderen\n" +
                "\tAufbau: [ip-Adresse im Format xxx.xxx.xxx.xxx] [port]\n" +
                "\tFehler: ungültige Ip-Adresse oder port, ungültige Formatierung";
    }


}
