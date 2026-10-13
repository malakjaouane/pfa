package ma.it6.portefeuille.dto;

/**
 * Synthèse d'un utilisateur (utilisée pour le responsable d'une initiative).
 */
public record UserSummaryDto(
        Long idUser,
        String nomComplet,
        String email
) {
}