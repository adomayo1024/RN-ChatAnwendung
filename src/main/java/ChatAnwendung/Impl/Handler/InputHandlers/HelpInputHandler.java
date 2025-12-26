package ChatAnwendung.Impl.Handler.InputHandlers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelpInputHandler extends AbstractInputHandler {


    public HelpInputHandler(String[] command) {
        super(command);
    }

    @Override
    public void run(){

        StringBuilder builder = new StringBuilder();

        if(command.length < 2){
            builder.append(HelpInputHandler.help());
            builder.append(ExitInputHandler.help());
            builder.append(FileInputHandler.help());
            builder.append(GoodbyeInputHandler.help());
            builder.append(HelloInputHandler.help());
            builder.append(MessageInputHandler.help());
            builder.append(ConnectHandler.help());
            builder.append(DisconnectHandler.help());
            builder.append(ListHandler.help());
        } else {
            switch (command[1]){
                case "help":
                    builder.append(HelpInputHandler.help());
                    break;
                case "exit":
                    builder.append(ExitInputHandler.help());
                    break;
                case "file":
                    builder.append(FileInputHandler.help());
                    break;
                case "bye":
                    builder.append(GoodbyeInputHandler.help());
                case "hello":
                    builder.append(HelloInputHandler.help());
                    break;
                case "send":
                    builder.append(MessageInputHandler.help());
                    break;
                case "connect":
                    builder.append(ConnectHandler.help());
                    break;
                case "disconnect":
                    builder.append(DisconnectHandler.help());
                    break;
                case "list":
                    builder.append(ListHandler.help());
                    break;
                default:
                    builder.append("Unbekannte command: ").append(command[1]);
            }
        }

        System.out.println(builder);

    }

    public static String help() {

        return """
                help: Zeigt alle Commands und deren erklärungen an
                \tAufbau: help <<[command]>>
                """;
    }
}
