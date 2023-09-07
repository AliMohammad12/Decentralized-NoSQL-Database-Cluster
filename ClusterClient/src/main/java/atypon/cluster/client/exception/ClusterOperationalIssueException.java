package atypon.cluster.client.exception;

public class ClusterOperationalIssueException extends RuntimeException {
    public ClusterOperationalIssueException() {
        super("The Cluster is encountering functionality problems. Please retry later!");
    }
}
