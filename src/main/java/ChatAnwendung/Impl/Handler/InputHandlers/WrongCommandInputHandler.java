package ChatAnwendung.Impl.Handler.InputHandlers;

public class WrongCommandInputHandler extends AbstractInputHandler {


    public WrongCommandInputHandler(String[] command) {
        super(command, WrongCommandInputHandler.class.getName());
    }


    @Override
    public void run(){
        System.out.println("Unknown Command: " + command[0]);
    }
}
