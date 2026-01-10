package ChatAnwendung.logic.Enums;

/**
 * Alle gültigen Commands, die der User in der Konsole eingeben kann.
 */
public enum InputCommands {
    SEND,
    FILE,
    HELLO,
    BYE,
    EXIT,
    CONNECT,
    DISCONNECT,
    LIST,
    HELP,
    INFO;


    /**
     * Prüft, ob ein Command verwendet werden darf, wenn der User abgemeldet ist.
     * @return True, wenn der Command verwendet werden darf, sonst false.
     */
    public boolean isLogOutCommand(){
        return !this.equals(SEND) && !this.equals(FILE) && !this.equals(BYE);

    }

    /**
     * Gibt alle Help Texte alle Commands wieder
     * @return Alle Help Texte als String.
     */
    public static String getAllHelpTexts(){
        StringBuilder sb = new StringBuilder();
        for(InputCommands command : InputCommands.values()){
            sb.append(command.getHelpText()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Gibt den Help Text für den spezifischen Command wieder.
     * @return Den Help Text des Commands.
     */
    public String getHelpText(){
        switch (this){
            case HELLO -> {
                return helloHelp();
            }
            case BYE -> {
                return goodbyeHelp();
            }
            case EXIT -> {
                return exitHelp();
            }
            case HELP -> {
                return helpHelp();
            }
            case INFO -> {
                return infoHelp();
            }
            case LIST -> {
                return listHelp();
            }
            case DISCONNECT -> {
                return disconnectHelp();
            }
            case CONNECT -> {
                return connectHelp();
            }
            case SEND -> {
                return messageHelp();
            }
            case FILE -> {
                return fileHelp();
            }
        }

        return "";
    }

    //------------ Help Text der Commands -------------

    private String infoHelp() {
        return """
                info: Gibt info über die eigene Id und auf welchen port man erreichbar ist.
                \tAufbau: info
                """;
    }

    private String listHelp() {
        return """
                list: Listet alle momentan erreichbaren Nutzer auf, oder alle derzeitigen Connections
                \tAufbau: list <<--all>> <<--connect>>
                """;
    }

    private String disconnectHelp() {
        return """
                disconnect: Disconnected diese Anwendung mit einer physischen Adresse und Port.
                \tAufbau: disconnect [ip-Address im Format xxx.xxx.xxx.xxx] [port]
                \tFehler: ungültige Ip-Adresse oder port, ungültige Formatierung
                """;
    }

    private String connectHelp() {
        return """
                connect: Verbindet diesen User direkt mit einen anderen
                \tAufbau: connect [ip-Adresse im Format xxx.xxx.xxx.xxx] [port]
                \tFehler: ungültige Ip-Adresse oder port, ungültige Formatierung
                """;

    }

    private String messageHelp() {
        return """
                send: Es wird eine Nachricht an einen bestimmten Teilnehmer geschickt. Die Nachricht darf maximal 1300 zeichen beinhalten (Weißzeichen mitgezählt)
                \tAufbau: send [EmpfängerID] "[Nachricht]"
                \tFehler: Wenn die UID falsch ist oder die Nachricht zu lange, wird keine Nachricht verschickt.
                """;

    }

    private String helloHelp() {
        return """
                hello: Der Hello command führt eine neu anmeldung durch. Dieser darf nur ausgeführt werden wenn man sich vorher abgemeldet hat mit den "bye" command.
                \tAufbau: hello
                \tFehler: Wenn man schon angemeldet ist, passiert nichts und dem User wird das durch eine Nachricht in Kenntnisse gesetzt
                """;


    }

    private String exitHelp() {
        return """
                exit: Meldet den User ab und beendet das Programm
                \tAufbau: exit
                \tFehler:
                """;

    }

    private String helpHelp() {
        return """
                help: Gibt infos über die vorhanden Commands oder ausgewählt eines einzelnen.
                \tAufbau: help <<command>>
                """;
    }

    private String goodbyeHelp() {
        return """
                bye: Meldet den User ab, er kann keine Nachrichten mehr schicken oder empfangen
                \tAufbau: bye
                \tFehler: Wenn man schon abgemeldet ist, kann man sich nicht nochmal abmelden
                """;

    }

    private String fileHelp() {
        return """
                file: Verschickt eine Datei die angegeben ist an einen bestimmten User
                \tAufbau: file  "[absoluter Datei Pfad]" [User Id]
                \tFehler: Die angegeben Datei gibt es nicht, der angegeben User ist nicht bekannt.\s
                """;
    }
}
