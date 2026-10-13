package ma.it6.portefeuille.dto;

import java.time.LocalDate;

/**
 * Filtres combinés de la liste des initiatives.
 * Tous les critères sont optionnels et cumulables.
 *
 * @param dateDu Date de début de la période « du … au … » : on retient les
 *               initiatives <strong>actives</strong> sur l'intervalle, c'est-à-
 *               dire celles dont la période de vie le chevauche.
 * @param dateAu Date de fin de la période « du … au … ».
 */
public record InitiativeFilterRequest(
        Long statut,
        Long entite,
        Long responsable,
        Long priorite,
        Long objectif,
        LocalDate dateDu,
        LocalDate dateAu,
        String recherche,
        Boolean archive
) {
}
