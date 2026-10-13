package ma.it6.portefeuille.dto;

/**
 * Statut d'une demande d'initiative (workflow).
 */
public record StatutDemandeDto(
        Long idStatutDemande,
        String libelle,
        Integer ordre
) {
}