package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;
import ChatAnwendung.Impl.Handler.InputHandlers.AbstractInputHandler;

public class ListHandler extends AbstractInputHandler {
    public ListHandler(String[] split) {
        super(split, ListHandler.class.getName());
    }

    public static String help() {
        return "list";
    }

    @Override
    public void run(){

    }
}
