package ma.it6.portefeuille.dto;

/**
 * Objectif stratégique associable à une initiative.
 */
public record ObjectifDto(
        Long idObjectif,
        String code,
        String libelle,
        String description
) {
}