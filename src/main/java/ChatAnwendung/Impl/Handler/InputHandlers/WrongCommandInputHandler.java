package ChatAnwendung.Impl.Handler.InputHandlers;

public class WrongCommandInputHandler extends AbstractInputHandler {


    public WrongCommandInputHandler(String[] command) {
        super(command, WrongCommandInputHandler.class.getName());
    }
}
