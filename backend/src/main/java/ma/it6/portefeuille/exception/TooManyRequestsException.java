package ma.it6.portefeuille.exception;

/**
 * Trop de tentatives (connexion ou autre action limitée) : réponse HTTP 429.
 */
public class TooManyRequestsException extends RuntimeException {

    public TooManyRequestsException(String message) {
        super(message);
    }
}
