package ma.it6.portefeuille.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Représentation d'une initiative renvoyée par l'API.
 *
 * <p>Le champ {@code budget} est le montant <em>saisi</em>. Aucun montant
 * dérivé n'est ajouté ici : il n'existe pas de saisie de dépenses réelles,
 * et afficher un « reste » calculé à côté de chaque initiative inviterait à
 * le lire comme un solde comptable. Les agrégats projetés (consommé, écart)
 * sont regroupés sur {@code GET /api/budget}, avec leurs définitions.</p>
 */
public record InitiativeResponse(
        Long idInitiative,
        String code,
        String nom,
        String description,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        BigDecimal budget,
        Integer avancement,
        Boolean archive,
        Boolean enRetard,
        EntiteDto entite,
        UserSummaryDto responsable,
        StatutInitiativeDto statut,
        PrioriteDto priorite,
        ObjectifDto objectif
) {
}