package aurea.epicstorysync.exception;

public class EpicException extends Exception {

    private static final long serialVersionUID = 1L;

    public EpicException(String message) {
        super(message);
    }

    public EpicException(String message, Throwable cause) {
        super(message, cause);
    }
}
