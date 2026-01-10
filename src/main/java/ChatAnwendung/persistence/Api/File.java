package ChatAnwendung.persistence.Api;

import ChatAnwendung.persistence.Impl.StorageImpl;

import java.net.DatagramPacket;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Repräsentiert eine Datei, die gerade heruntergeladen wird.
 */
public interface File {

    /**
     * Fügt einen neuen Chunk, zum File hinzu.
     * @param chunk Die Bytes des Chunks
     * @param sequenz Die Sequenz des Chunks, welcher angibt, an welcher Position es sich im File befindet.
     * @return True wenn der Chunk hinzugefügt wurde, sonst false.
     */
    boolean addChunk(byte[] chunk, int sequenz);

    /**
     * "Speichert" das File ab, in dem alle Chunks in eine pyhsiche Datei geschrieben wird.
     * Wenn es zu IOExceptions kommt, wird das File nicht gespeichert und gelöscht.
     */
    void safeFile();

    /**
     * Prüft, ob alle erwarteten Chunks vorhanden sind.
     * @return True, wenn alle Chunks vorhanden sind, sonst false.
     */
    boolean finished();

    /**
     * Gibt alle Sequenznummern zurück, die noch fehlen, damit alle Chunks, und somit die gesamte Datei, vorhanden ist.
     * @return Eine Liste mit allen Sequenznummern von den Chunks, die noch fehlen.
     */
    List<Integer> getMissingChunks();

    /**
     * Gibt den Zeitpunkt wieder, wo der neueste Chunk empfangen wurde.
     * @return Zeitpunkt in Millisekunden ind UNIX Epoch.
     */
    long getReceivedLastChunk();

    //-------------- GETER---------------------

    long getSrcNodeId();

    int getFileId();

    String getName();
}
