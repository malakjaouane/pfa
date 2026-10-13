package ma.it6.portefeuille.dto;

import java.time.LocalDate;

/**
 * Étape renvoyée par l'API.
 */
public record EtapeResponse(
        Long idEtape,
        String nom,
        String description,
        Integer ordre,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        Integer avancement,
        StatutEtapeDto statutEtape,
        Long idInitiative
) {
}