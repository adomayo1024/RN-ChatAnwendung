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
        }

        System.out.println(builder);

    }

    public static String help() {
        return "help:\n";
    }
}
