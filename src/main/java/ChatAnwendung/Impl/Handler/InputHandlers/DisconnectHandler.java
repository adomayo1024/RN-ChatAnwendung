package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.persistence.Connection;
import ChatAnwendung.Impl.persistence.ConnectionsList;
import ChatAnwendung.Impl.Exceptions.ArgumentException;
import ChatAnwendung.Impl.Handler.Common.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class DisconnectHandler extends AbstractInputHandler {
    public DisconnectHandler(String[] split) {
        super(split);
    }

    public static String help() {
        return "disconnect: Trennt eine direkte Verbindung zu diesem Host\n" +
                "\tAufbau: [ip-Address im format xxx.xxx.xxx.xxx] [port]\n" +
                "\tFehler: ungueltige Ip adresse oder port\n";
    }

    @Override
    public void run(){

        log.debug("start with disconnect: {} : {}", command[1], command[2]);

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

        log.debug("end with disconnect: {} : {}", command[1], command[2]);

    }

}
