package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;

public class WrongCommandHandler extends AbstractHandler {


    public WrongCommandHandler(String[] command) {
        super(command, WrongCommandHandler.class.getName());
    }
}
