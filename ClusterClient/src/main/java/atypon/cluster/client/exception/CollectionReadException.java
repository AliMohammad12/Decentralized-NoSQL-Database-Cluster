package atypon.cluster.client.exception;

public class CollectionReadException extends RuntimeException{
    public CollectionReadException(String message) {
        super("Failed to read the collection: " + message);
    }
}
