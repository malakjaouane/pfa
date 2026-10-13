package ma.it6.portefeuille.dto;

import java.time.LocalDate;

/**
 * Jalon renvoyé par l'API.
 *
 * @param enRetard {@code true} si la date prévue est passée et que le jalon
 *                 n'est pas « Atteint » (calculé côté serveur).
 */
public record JalonResponse(
        Long idJalon,
        String nom,
        String description,
        LocalDate datePrevue,
        LocalDate dateRealisation,
        StatutJalonDto statutJalon,
        Long idInitiative,
        Boolean enRetard
) {
}
