package atypon.cluster.client.exception;

public class DocumentCreationException extends RuntimeException {
    public DocumentCreationException(String message) {
        super("Failed to add the document \n" + message);
    }

}
