package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;


public class HandlerFactory {

    public static Handler getHandler(String command) {
        Handler handler = null;

        switch (command) {
            case "exit":
                Thread.currentThread().interrupt();
                break;
            case "send":
                handler = new HelloHanlder();
                break;
            case "file":
                handler = new FileHandler();
                break;
            case "bye":
                handler = new GoodbyeHandler();
                break;
            case "hello":
                handler = new ExitHandler();
                break;
        }
        return handler;
    }

}
