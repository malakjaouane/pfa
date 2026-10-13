package ma.it6.portefeuille.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Vue budgétaire globale du portefeuille.
 *
 * <p>Quatre grandeurs, avec une définition unique et explicite :</p>
 * <ul>
 *   <li><b>budgetTotal</b> — somme des budgets engagés ;</li>
 *   <li><b>consommé</b> — budget × avancement : une <strong>projection</strong>
 *       de ce que l'avancement laisse supposer consommé. Ce n'est PAS une
 *       comptabilité : le cahier des charges ne prévoit aucune saisie de
 *       dépenses réelles, aucune facture n'est rattachée. À présenter comme
 *       un indicateur de pilotage, jamais comme un montant dépensé ;</li>
 *   <li><b>budgetRestant</b> = budgetTotal − consommé. C'est la grandeur qui
 *       se <em>recoupe</em> : budgetTotal = consommé + budgetRestant, à la
 *       décimale près. Un pilote qui additionne les deux colonnes d'un écran
 *       doit retrouver le total, sinon il cesse de faire confiance au reste.</li>
 *   <li><b>effectif</b> — budget des initiatives « Terminée » ou « Annulée » :
 *       dépense réellement engagée, indépendamment de l'avancement ;</li>
 *   <li><b>écart</b> = effectif − consommé, et son pourcentage par rapport au
 *       consommé (null quand le consommé est nul, pour éviter une division par
 *       zéro sans masquer le cas). Un écart négatif indique qu'on a plus
 *       « consommé au prorata » que de dépenses définitivement closes — le cas
 *       d'un portefeuille chargé d'initiatives encore en cours.</li>
 * </ul>
 *
 * @param parStatut   répartition par statut d'initiative
 * @param parEntite   répartition par entité de rattachement
 * @param parObjectif répartition par objectif stratégique
 * @param parPriorite répartition par priorité
 */
public record BudgetGlobalResponse(
        BigDecimal budgetTotal,
        BigDecimal consomme,
        BigDecimal budgetRestant,
        BigDecimal effectif,
        BigDecimal ecart,
        BigDecimal ecartPourcentage,
        Integer nbInitiatives,
        Integer nbInitiativesEnCours,
        List<Repartition> parStatut,
        List<Repartition> parEntite,
        List<Repartition> parObjectif,
        List<Repartition> parPriorite
) {

    /**
     * Ligne d'une répartition. {@code cle} est l'identifiant de regroupement
     * (id d'entité, d'objectif…) ; {@code libelle} est ce qui s'affiche.
     * Les initiatives sans objectif sont regroupées sous la clé « sans ».
     */
    public record Repartition(
            String cle,
            String libelle,
            Integer nbInitiatives,
            BigDecimal budget,
            BigDecimal consomme,
            BigDecimal effectif,
            BigDecimal ecart
    ) {
    }
}
