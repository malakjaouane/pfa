package ma.it6.portefeuille.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corps d'erreur renvoyé par l'API REST en cas d'échec.
 */
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
    public ApiError(int status, String error, String message, List<String> details) {
        this(LocalDateTime.now(), status, error, message, details);
    }
}