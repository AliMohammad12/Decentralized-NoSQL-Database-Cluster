package atypon.cluster.client.exception;
public class InvalidUserCredentialsException extends RuntimeException {
    public InvalidUserCredentialsException() {
        super("Invalid credentials! Please use correct credentials");
    }
}