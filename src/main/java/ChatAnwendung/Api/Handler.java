package ChatAnwendung.Api;

public interface Handler extends Runnable{

    @Override
    default void run(){
        System.out.println("Hi");
    }

    public static String help() {
        return null;
    }
}
