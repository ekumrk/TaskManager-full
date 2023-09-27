package exceptions;

public class RegistrationAPIException extends RuntimeException {
    public RegistrationAPIException(String message) {
        super(message);
    }
}
