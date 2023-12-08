package atypon.app.node.response;

public class ValidatorResponse {
    private String message;
    private boolean valid;
    public ValidatorResponse(boolean valid) {
        this.valid = valid;
    }
    public ValidatorResponse(String message, boolean valid) {
        this.message = message;
        this.valid = valid;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isValid() {
        return valid;
    }
    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
