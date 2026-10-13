package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corps de création / modification d'un objectif stratégique.
 */
public record ObjectifRequest(
        @NotBlank(message = "Le code est obligatoire") String code,
        @NotBlank(message = "Le libellé est obligatoire") String libelle,
        String description
) {
}