package ma.it6.portefeuille.dto;

import java.time.LocalDateTime;

/**
 * Événement du journal des transitions de statut d'une initiative.
 *
 * @param ancienStatut statut avant la transition, {@code null} à la création
 * @param utilisateur  auteur de la transition (peut être {@code null} si le
 *                     compte a été supprimé entre-temps)
 */
public record HistoriqueStatutResponse(
        Long idHistorique,
        String ancienStatut,
        String nouveauStatut,
        String commentaire,
        LocalDateTime dateChangement,
        UserSummaryDto utilisateur
) {
}
