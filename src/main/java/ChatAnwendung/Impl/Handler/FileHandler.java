package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;

public class FileHandler extends AbstractHandler {
    public FileHandler(String[] command) {
        super(command, FileHandler.class.getName());
    }

    @Override
    public void run() {
        System.out.println("FileHandler");
    }

    public static String help(){
        return "file:\n";
    }

}
