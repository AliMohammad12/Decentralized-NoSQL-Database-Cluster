package atypon.cluster.client.exception;

public class DocumentDeletionException  extends  RuntimeException{
    public DocumentDeletionException(String message) {
        super("Failed to delete the document with id  '" + message + "'.");
    }

}
