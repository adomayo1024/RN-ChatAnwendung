package ChatAnwendung.Impl.Handler.InputHandlers;

public class DisconnectHandler extends AbstractInputHandler {
    public DisconnectHandler(String[] split) {
        super(split, DisconnectHandler.class.getName());
    }

    public static String help() {
        return "no";
    }

    @Override
    public void run(){

    }

}
