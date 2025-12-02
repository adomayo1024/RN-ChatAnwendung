package ChatAnwendung.Impl.Handler;

import ChatAnwendung.Api.Handler;

public class ExitHandler extends AbstractHandler {


    Thread inputReaderThread;

    public ExitHandler(Thread thread, String[] command) {
        super(command, ExitHandler.class.getName());
        inputReaderThread = thread;
    }

    @Override
    public void run() {

        inputReaderThread.interrupt();
    }


    public static String help(){
        return "exit:\n";
    }
}
