package atypon.cluster.client.exception;
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials! Please use correct credentials");
    }
}