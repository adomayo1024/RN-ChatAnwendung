package ChatAnwendung.Impl.Handler.InputHandlers;

public class ConnectHandler extends AbstractInputHandler {
    public ConnectHandler(String[] split) {
        super(split, ConnectHandler.class.getName());
    }

    public static String help() {
        return "connect <ip> <port>";
    }

    @Override
    public void run(){

    }
}
