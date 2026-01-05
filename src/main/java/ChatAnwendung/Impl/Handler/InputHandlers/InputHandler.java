package ChatAnwendung.Impl.Handler.InputHandlers;

import ChatAnwendung.Impl.InputCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class InputHandler implements Runnable {

    private final BlockingQueue<String> inputQueue;


    public InputHandler(BlockingQueue<String> inputQueue) {
        this.inputQueue = inputQueue;
    }

    @Override
    public void run() {

        boolean interrupted = false;
        String input;

        while (!interrupted){

            String[] command = new String[1];
            InputCommands commandType;

            try {
                input = inputQueue.take();
                command = input.split(" ");
                commandType = InputCommands.valueOf(command[0].toUpperCase());
            } catch (InterruptedException e) {
                interrupted = true;
                continue;
            } catch (IllegalArgumentException e){
                System.out.print("Unknown Command: " + command[0]);
                continue;
            }

            if(!commandType.isLogOutCommand()){
                continue;
            }

            switch (commandType){
                case InputCommands.CONNECT -> handleConnect(command);

                case InputCommands.DISCONNECT -> handleDisconnect(command);

                case InputCommands.HELLO -> handleHello(command);

                case InputCommands.GOODBYE -> handleGoodbye(command);

                case InputCommands.SEND -> handleSend(command);

                case InputCommands.FILE -> handleFile(command);

                case InputCommands.LIST -> handleList(command);

                case InputCommands.EXIT -> handleExit(command);

                case InputCommands.HELP -> handleHelp(command);
            }

        }

    }

    private void handleHelp(String[] command) {
        StringBuilder builder = new StringBuilder();

        if(command.length < 2){
            builder.append(HelpInputHandler.help());
            builder.append(exitHelp());
            builder.append(fileHelp());
            builder.append(goodbyeHelp());
            builder.append(helloHelp());
            builder.append(messageHelp());
            builder.append(connectHelp());
            builder.append(disconnectHelp());
            builder.append(listHelp());
        } else {
            switch (InputCommands.valueOf(command[1].toUpperCase())){
                case InputCommands.HELP-> builder.append(helpHelp());

                case InputCommands.EXIT -> builder.append(exitHelp());

                case InputCommands.FILE -> builder.append(fileHelp());

                case InputCommands.GOODBYE -> builder.append(goodbyeHelp());

                case InputCommands.HELLO -> builder.append(HelloInputHandler.help());

                case InputCommands.SEND -> builder.append(messageHelp());

                case InputCommands.CONNECT -> builder.append(ConnectHandler.help());

                case InputCommands.DISCONNECT -> builder.append(disconnectHelp());

                case InputCommands.LIST -> builder.append(listHelp());
            }
        }

        System.out.println(builder);
    }

    private String listHelp() {
        return null;
    }

    private String disconnectHelp() {
        return null;
    }

    private String connectHelp() {
        return null;
    }

    private String messageHelp() {
        return null;
    }

    private String helloHelp() {
        return null;
    }

    private String exitHelp() {
        return null;
    }

    private String helpHelp() {
        return null;
    }

    private String goodbyeHelp() {
        return null;
    }

    private String fileHelp() {
        return null;
    }

    private void handleExit(String[] command) {
    }

    private void handleList(String[] command) {
    }

    private void handleFile(String[] command) {
    }

    private void handleSend(String[] command) {
    }

    private void handleGoodbye(String[] command) {
    }

    private void handleHello(String[] command) {
    }

    private void handleDisconnect(String[] command) {
    }

    private void handleConnect(String[] command) {
    }
}
