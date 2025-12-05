package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;


public class HandlerFactory {

    public static Handler getHandler(String stdIn) {

        String[] split = splitInput(stdIn);
        String command = split[0];

        Handler handler;
        switch (command) {
            case "exit":
                handler = new ExitHandler(split);
                break;
            case "send":
                handler = new MessageHandler(split);
                break;
            case "file":
                handler = new FileHandler(split);
                break;
            case "bye":
                handler = new GoodbyeHandler(split);
                break;
            case "hello":
                handler = new HelloHandler(split);
                break;
            case "help":
                handler = new HelpHandler(split);
                break;
            case "":
                System.out.println("Nicht angemeldet");
            default:
                handler = new WrongCommandHandler(split);
                break;
        }
        return handler;
    }

    private static String[] splitInput(String stdIn){
        String[] split = stdIn.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].replaceAll("^\"|\"$", "");
        }

        return split;
    }


}
