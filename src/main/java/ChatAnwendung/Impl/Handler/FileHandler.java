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

        return "file: Verschickt eine Datei die angegeben ist an einen bestimmten User\n" +
                "\tAufbau: file [absoluter Datei Pfad] [User Id]\n" +
                "\tFehler: Die angegeben Datei gibt es nicht, der angegeben User ist nicht bekannt. \n";
    }

}
