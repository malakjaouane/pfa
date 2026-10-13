package ma.it6.portefeuille.service;

import ma.it6.portefeuille.dto.BudgetEntiteResponse;
import ma.it6.portefeuille.dto.BudgetGlobalResponse;
import ma.it6.portefeuille.dto.BudgetGlobalResponse.Repartition;
import ma.it6.portefeuille.dto.InitiativeFilterRequest;
import ma.it6.portefeuille.entity.Entite;
import ma.it6.portefeuille.entity.Initiative;
import ma.it6.portefeuille.repository.EntiteRepository;
import ma.it6.portefeuille.repository.InitiativeRepository;
import ma.it6.portefeuille.specification.InitiativeSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Agrégats budgétaires du portefeuille.
 *
 * <p><b>À lire avant tout, y compris à la soutenance : ces chiffres sont des
 * PROJECTIONS, pas une comptabilité.</b> Le cahier des charges ne prévoit
 * aucune saisie de dépenses réelles — pas de table de mouvements, pas de
 * factures. « consommé » est donc estimé au prorata de l'avancement
 * ({@code budget × avancement / 100}). C'est un indicateur de pilotage, à
 * présenter comme tel : on n'affirme pas que tel montant a été dépensé, on
 * indique à quel point le portefeuille est avancé rapporté aux budgets
 * engagés.</p>
 *
 * <p>Les chiffres sont calculés en Java sur la <strong>même sélection
 * filtrée</strong> que la liste des initiatives et le dashboard : il est donc
 * impossible d'afficher un budget qui ne correspond à aucun filtre visible.
 * Ce parti pris — et non des requêtes SQL d'agrégation — maintient une seule
 * source de vérité pour les définitions (consommé, effectif, écart).</p>
 *
 * <p>Le budget est un <em>champ de saisie</em> : il n'existe pas de table de
 * mouvements financiers dans le cahier des charges. Les grandeurs ci-dessous
 * en sont donc des dérivés, dont la définition est documentée dans
 * {@link BudgetGlobalResponse}.</p>
 */
@Service
public class BudgetService {

    private static final BigDecimal CENT = new BigDecimal("100");

    private final InitiativeRepository initiativeRepository;
    private final EntiteRepository entiteRepository;

    public BudgetService(InitiativeRepository initiativeRepository,
                         EntiteRepository entiteRepository) {
        this.initiativeRepository = initiativeRepository;
        this.entiteRepository = entiteRepository;
    }

    /**
     * Vue budgétaire globale, avec les réparutions par statut, entité,
     * objectif et priorité.
     */
    @Transactional(readOnly = true)
    public BudgetGlobalResponse global(InitiativeFilterRequest filtre) {
        List<Initiative> initiatives = selection(filtre);

        BigDecimal budgetTotal = BigDecimal.ZERO;
        BigDecimal consomme = BigDecimal.ZERO;
        BigDecimal effectif = BigDecimal.ZERO;
        int enCours = 0;

        for (Initiative i : initiatives) {
            budgetTotal = budgetTotal.add(budget(i));
            consomme = consomme.add(consomme(i));
            effectif = effectif.add(effectif(i));
            if (estEnCours(i)) {
                enCours++;
            }
        }

        BigDecimal ecart = effectif.subtract(consomme);
        // Sans division par zéro : un portefeuille entièrement à 0 % n'a pas
        // de taux d'écart, et dire « 0 % » serait trompeur.
        BigDecimal ecartPourcentage = consomme.signum() == 0
                ? null
                : ecart.multiply(CENT).divide(consomme, 1, RoundingMode.HALF_UP);

        return new BudgetGlobalResponse(
                arrondi(budgetTotal),
                arrondi(consomme),
                arrondi(budgetTotal.subtract(consomme)),
                arrondi(effectif),
                arrondi(ecart),
                ecartPourcentage,
                initiatives.size(),
                enCours,
                regrouper(initiatives, i -> cle(i.getStatut() == null ? null : i.getStatut().getIdStatut(),
                        i.getStatut() == null ? "Non défini" : i.getStatut().getLibelle())),
                regrouper(initiatives, i -> cle(i.getEntite() == null ? null : i.getEntite().getIdEntite(),
                        i.getEntite() == null ? "Non affectée" : i.getEntite().getNom())),
                regrouper(initiatives, i -> cle(i.getObjectif() == null ? null : i.getObjectif().getIdObjectif(),
                        i.getObjectif() == null ? "Non rattaché" : i.getObjectif().getLibelle())),
                regrouper(initiatives, i -> cle(i.getPriorite() == null ? null : i.getPriorite().getIdPriorite(),
                        i.getPriorite() == null ? "Non définie" : i.getPriorite().getLibelle())));
    }

    /**
     * Budget par entité : chaque entité de rattachement apparaît, même sans
     * initiative, afin qu'un pilote voie l'absence de portefeuille plutôt que
     * la disparition de la ligne.
     */
    @Transactional(readOnly = true)
    public List<BudgetEntiteResponse> parEntite(InitiativeFilterRequest filtre) {
        Map<Long, Initiateurs> parEntite = new LinkedHashMap<>();

        // Toutes les entités, y compris celles sans initiative.
        for (Entite entite : entiteRepository.findAll()) {
            parEntite.put(entite.getIdEntite(), new Initiateurs(entite));
        }

        for (Initiative i : selection(filtre)) {
            if (i.getEntite() == null) {
                continue;
            }
            Initiateurs accumulateur = parEntite.computeIfAbsent(
                    i.getEntite().getIdEntite(), id -> new Initiateurs(i.getEntite()));
            accumulateur.ajouter(i);
        }

        return parEntite.values().stream()
                .map(Initiateurs::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // Grandeurs
    // ------------------------------------------------------------------

    private static BigDecimal budget(Initiative i) {
        return i.getBudget() == null ? BigDecimal.ZERO : i.getBudget();
    }

    /** Dépense déjà consommée, au prorata de l'avancement. */
    private static BigDecimal consomme(Initiative i) {
        int avancement = i.getAvancement() == null ? 0 : i.getAvancement();
        return budget(i).multiply(BigDecimal.valueOf(avancement)).divide(CENT, 2, RoundingMode.HALF_UP);
    }

    /** Dépense réellement engagée : le budget des initiatives closes. */
    private static BigDecimal effectif(Initiative i) {
        String statut = i.getStatut() == null ? null : i.getStatut().getLibelle();
        return InitiativeService.STATUT_TERMINEE.equals(statut)
                || InitiativeService.STATUT_ANNULEE.equals(statut)
                ? budget(i)
                : BigDecimal.ZERO;
    }

    /**
     * Initiative en cours de réalisation : non close et non annulée. Le
     * portefeuille « en cours » est l'objet du pilotage au quotidien.
     */
    private static boolean estEnCours(Initiative i) {
        String statut = i.getStatut() == null ? null : i.getStatut().getLibelle();
        return !InitiativeService.STATUT_TERMINEE.equals(statut)
                && !InitiativeService.STATUT_ANNULEE.equals(statut);
    }

    // ------------------------------------------------------------------
    // Regroupement
    // ------------------------------------------------------------------

    private List<Initiative> selection(InitiativeFilterRequest filtre) {
        return initiativeRepository.findAll(InitiativeSpecifications.filtrer(filtre));
    }

    /**
     * Clé de regroupement : l'identifiant sert de clé technique (affichable
     * tel quel dans un filtre), le libellé de ce qui s'affiche. Un identifiant
     * absent est regroupé sous la clé « sans », afin que le total des lignes
     * soit toujours égal au total global — sinon l'utilisateur perdrait des
     * initiatives sans s'en apercevoir.
     */
    private static Cle cle(Long id, String libelle) {
        return id == null
                ? new Cle("sans", libelle)
                : new Cle(String.valueOf(id), libelle);
    }

    private record Cle(String cle, String libelle) {
    }

    /**
     * Regroupe puis additionne : une seule boucle par dimension, et l'ordre
     * d'apparition est préservé (LinkedHashMap) pour un affichage stable.
     */
    private static List<Repartition> regrouper(List<Initiative> initiatives,
                                               Function<Initiative, Cle> extracteur) {
        Map<String, Accumulateur> groupes = new LinkedHashMap<>();
        for (Initiative i : initiatives) {
            Cle cle = extracteur.apply(i);
            Accumulateur acc = groupes.computeIfAbsent(cle.cle(), k -> new Accumulateur(cle.libelle()));
            acc.ajouter(i);
        }
        return groupes.entrySet().stream()
                .map(e -> e.getValue().toRepartition(e.getKey()))
                .toList();
    }

    /** Accumulateur d'agrégats budgétaires pour un groupe d'initiatives. */
    private static final class Accumulateur {
        private final String libelle;
        private int nb;
        private BigDecimal budget = BigDecimal.ZERO;
        private BigDecimal consomme = BigDecimal.ZERO;
        private BigDecimal effectif = BigDecimal.ZERO;

        private Accumulateur(String libelle) {
            this.libelle = libelle;
        }

        private void ajouter(Initiative i) {
            nb++;
            budget = budget.add(BudgetService.budget(i));
            consomme = consomme.add(BudgetService.consomme(i));
            effectif = effectif.add(BudgetService.effectif(i));
        }

        private Repartition toRepartition(String cle) {
            return new Repartition(cle, libelle, nb,
                    arrondi(budget), arrondi(consomme), arrondi(effectif), arrondi(effectif.subtract(consomme)));
        }
    }

    /** Accumulateur spécialisé « par entité ». */
    private static final class Initiateurs {
        private final Entite entite;
        private int nb;
        private BigDecimal budget = BigDecimal.ZERO;
        private BigDecimal consomme = BigDecimal.ZERO;
        private BigDecimal effectif = BigDecimal.ZERO;

        private Initiateurs(Entite entite) {
            this.entite = entite;
        }

        private void ajouter(Initiative i) {
            nb++;
            budget = budget.add(BudgetService.budget(i));
            consomme = consomme.add(BudgetService.consomme(i));
            effectif = effectif.add(BudgetService.effectif(i));
        }

        private BudgetEntiteResponse toResponse() {
            BigDecimal ecart = effectif.subtract(consomme);
            BigDecimal taux = budget.signum() == 0
                    ? BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP)
                    : consomme.multiply(CENT).divide(budget, 1, RoundingMode.HALF_UP);
            return new BudgetEntiteResponse(entite.getIdEntite(), entite.getCode(), entite.getNom(),
                    nb, arrondi(budget), arrondi(consomme), arrondi(effectif), arrondi(ecart), taux);
        }
    }

    private static BigDecimal arrondi(BigDecimal valeur) {
        return valeur.setScale(2, RoundingMode.HALF_UP);
    }
}
