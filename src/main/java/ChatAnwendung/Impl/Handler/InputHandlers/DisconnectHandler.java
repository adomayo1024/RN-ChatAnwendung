package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Connection;
import ChatAnwendung.Impl.ConnectionsList;
import ChatAnwendung.Impl.Exceptions.ArgumentException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DisconnectHandler extends AbstractInputHandler {
    public DisconnectHandler(String[] split) {
        super(split, DisconnectHandler.class.getName());
    }

    public static String help() {
        return "disconnect: Trennt eine direkte Verbindung zu diesem Host\n" +
                "\tAufbau: [ip-Address im format xxx.xxx.xxx.xxx] [port]\n" +
                "\tFehler: ungueltige Ip adresse oder port\n";
    }

    @Override
    public void run(){

        InetAddress address;
        int port;

        try {
            address = InetAddress.getByName(command[1]);
            port = Integer.parseInt(command[2]);
            Connection connection = new Connection(address, port);

            ConnectionsList.getInstance().remove(connection);
        } catch (UnknownHostException e) {
            ExceptionHandler.handle(new ArgumentException(e.getMessage()), this.getClass());
        }

    }

}
