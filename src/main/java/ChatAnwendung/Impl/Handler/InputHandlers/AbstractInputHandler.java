package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.Handler.Common.AbstractHandler;

public abstract class AbstractInputHandler extends AbstractHandler {

    protected String[] command;


    public AbstractInputHandler(String[] command, String name){
        super(name);
        this.command = command;
    }
}
