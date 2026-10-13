package ma.it6.portefeuille.dto;

import java.time.LocalDate;

/**
 * Initiative issue de la transformation d'une demande acceptée.
 */
public record InitiativeLieeDto(
        Long idInitiative,
        String code,
        String nom
) {
}