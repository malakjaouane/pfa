package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Corps de création / modification d'une demande d'initiative.
 * Le demandeur et la date de soumission sont déterminés par le serveur
 * (utilisateur connecté, date du jour).
 */
public record DemandeRequest(
        @NotBlank(message = "Le titre est obligatoire") String titre,
        String descriptionBesoin,
        String justificationMetier,
        @NotNull(message = "L'entité demandeuse est obligatoire") Long idEntite,
        @NotNull(message = "La priorité est obligatoire") Long idPriorite
) {
}