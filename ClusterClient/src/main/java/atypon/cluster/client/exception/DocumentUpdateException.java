package atypon.cluster.client.exception;

public class DocumentUpdateException extends RuntimeException {
    public DocumentUpdateException(String message) {
        super("Failed to update the document!\n" + message);
    }
}
