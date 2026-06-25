package exception;

public class ExpiredProductException extends RuntimeException {
    public ExpiredProductException(String message) {
        super(message);
    }
}
