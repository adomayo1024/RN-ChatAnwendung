package ChatAnwendung.persistence.Api;

import java.io.BufferedReader;

public interface Storage {

    /**
     * Fügt eine neue Datei, die verschickt wurde, hinzu.
     * @param fileId Die Id der Datei
     * @param path Der Pfad zu der Datei
     */
    void addSendOpenFile(int fileId, String path);

    /**
     * Gibt die nächste File Id wieder, die verwendet werden soll, für das Sendern der nächsten Datei.
     * @return Die Id für die nächste zu versendende Datei.
     */
    int getNextFileID();

    /**
     * Loggt den User ein um Nachrichten und Dateien zu empfangen und zu senden.
     */
    void login();

    /**
     * Loggt den User aus, um keine Nachrichten und Dateien mehr zu empfangen und zu senden.
     */
    void logout();

    /**
     * Gibt eine String repräsentation der Node Id des Hosts zurück als Unsigned Long.
     * @return Unsigned Node Id des Hosts.
     */
    String getUnsignedID();

    /**
     * Gibt den Pfad der Datei mit dieser File Id zurück, die versendet wurde.
     * @param fileID Die Id der Datei.
     * @return Der Pfad der Datei.
     */
    String getOpenFile(int fileID);


    //------------- GETTER -------------

    Long getBroadCastId();

    long getID();

    int getPort();

    boolean isLogin();

    BufferedReader getReader();

    //------------- SETTER -------------

    void setPort(int port);

    void setReader(BufferedReader reader);

}
