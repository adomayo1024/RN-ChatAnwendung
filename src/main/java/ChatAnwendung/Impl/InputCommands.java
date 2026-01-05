package ChatAnwendung.Impl;

public enum InputCommands {
    SEND,
    FILE,
    HELLO,
    GOODBYE,
    EXIT,
    CONNECT,
    DISCONNECT,
    LIST,
    HELP;


    public boolean isLogOutCommand(){
        return !this.equals(SEND) && !this.equals(FILE) && !this.equals(GOODBYE);

    }



}
