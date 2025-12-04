package ChatAnwendung.Impl.Handler;

public class HelpHandler extends AbstractHandler {


    public HelpHandler(String[] command) {
        super(command, HelpHandler.class.getName());
    }

    @Override
    public void run(){

        StringBuilder builder = new StringBuilder();

        if(command.length < 2){
            builder.append(HelpHandler.help());
            builder.append(ExitHandler.help());
            builder.append(FileHandler.help());
            builder.append(GoodbyeHandler.help());
            builder.append(HelloHandler.help());
            builder.append(MessageHandler.help());
        } else {
            switch (command[1]){
                case "help":
                    builder.append(HelpHandler.help());
                    break;
                case "exit":
                    builder.append(ExitHandler.help());
                    break;
                case "file":
                    builder.append(FileHandler.help());
                    break;
                case "bye":
                    builder.append(GoodbyeHandler.help());
                case "hello":
                    builder.append(HelloHandler.help());
                    break;
                case "send":
                    builder.append(MessageHandler.help());
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
