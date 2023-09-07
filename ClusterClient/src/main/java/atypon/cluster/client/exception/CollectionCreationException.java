package atypon.cluster.client.exception;

public class CollectionCreationException extends Exception {
    public CollectionCreationException(String message) {
        super("Failed to create the collection: " + message);
    }
}
