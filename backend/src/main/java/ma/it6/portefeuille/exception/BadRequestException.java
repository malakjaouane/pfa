package ma.it6.portefeuille.exception;

/**
 * Levée lorsqu'une règle métier ou une validation est violée (HTTP 400).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}