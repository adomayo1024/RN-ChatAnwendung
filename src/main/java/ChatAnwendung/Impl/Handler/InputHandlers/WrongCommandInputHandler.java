package ChatAnwendung.Impl.Handler.InputHandlers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WrongCommandInputHandler extends AbstractInputHandler {


    public WrongCommandInputHandler(String[] command) {
        super(command);
    }


    @Override
    public void run(){

        System.out.println("Unknown Command: " + command[0]);
    }
}
