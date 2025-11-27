package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;

public class MessageHandler implements Runnable, Handler {

    String command;

    public void setCommand(String command){
        this.command = command;
    }

    @Override
    public void run() {

    }
}
