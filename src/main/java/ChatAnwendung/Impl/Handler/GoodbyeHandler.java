package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;

public class GoodbyeHandler extends AbstractHandler {
    public GoodbyeHandler(String[] command) {
        super(command, GoodbyeHandler.class.getName());
    }

    @Override
    public void run() {

    }

    public static String help(){
        return "bye:\n";
    }
}
