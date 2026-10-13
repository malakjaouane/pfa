package ma.it6.portefeuille.service;

import ma.it6.portefeuille.dto.BudgetEntiteResponse;
import ma.it6.portefeuille.dto.BudgetGlobalResponse;
import ma.it6.portefeuille.dto.InitiativeFilterRequest;
import ma.it6.portefeuille.entity.Entite;
import ma.it6.portefeuille.entity.Initiative;
import ma.it6.portefeuille.entity.Priorite;
import ma.it6.portefeuille.entity.StatutInitiative;
import ma.it6.portefeuille.repository.EntiteRepository;
import ma.it6.portefeuille.repository.InitiativeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Agrégats budgétaire : les définitions (consommé, effectif, écart) sont
 * <strong>le contrat</strong> entre le backend et les écrans. Elles sont
 * testées ici telles qu'écrites dans le cahier des charges, parce qu'un
 * décalage de 1 % entre deux écrans financiers se remarque bien plus tard,
 * et coûte bien plus cher, qu'une erreur de compilation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BudgetService — consommé, effectif et écart")
class BudgetServiceTest {

    @Mock
    private InitiativeRepository initiativeRepository;
    @Mock
    private EntiteRepository entiteRepository;

    @InjectMocks
    private BudgetService service;

    private final InitiativeFilterRequest aucunFiltre = new InitiativeFilterRequest(
            null, null, null, null, null, null, null, null, null);

    private Entite it6;
    private Entite directionDigitale;

    @BeforeEach
    void setUp() {
        it6 = entite(1L, "IT6", "IT6 Consulting");
        directionDigitale = entite(2L, "DV", "Direction Digitale");
        when(entiteRepository.findAll()).thenReturn(List.of(it6, directionDigitale));
    }

    @Nested
    @DisplayName("Grandeurs globales")
    class GrandeursGlobales {

        @Test
        @DisplayName("consommé = budget × avancement, effectif = budget des initiatives closes")
        void consommeEffectif() {
            // 100 000 à 40 %  -> consommé 40 000, effectif 0 (encore en cours)
            // 200 000 à 100 % -> consommé 200 000, effectif 200 000 (Terminée)
            // 300 000 à 0 %   -> consommé 0, effectif 300 000 (Annulée)
            repondreAvec(
                    initiative(1L, "100000.00", 40, "En cours", it6),
                    initiative(2L, "200000.00", 100, "Terminée", it6),
                    initiative(3L, "300000.00", 0, "Annulée", it6));

            BudgetGlobalResponse r = service.global(aucunFiltre);

            assertThat(r.budgetTotal()).isEqualByComparingTo("600000.00");
            assertThat(r.consomme()).isEqualByComparingTo("240000.00");
            assertThat(r.effectif()).isEqualByComparingTo("500000.00");
            assertThat(r.nbInitiatives()).isEqualTo(3);
            // « en cours » = ni Terminée ni Annulée.
            assertThat(r.nbInitiativesEnCours()).isEqualTo(1);
            // Recoupement : 600 000 = 240 000 consommés + 360 000 restants.
            assertThat(r.budgetRestant()).isEqualByComparingTo("360000.00");
        }

        @Test
        @DisplayName("écart = effectif − consommé, négatif quand les initiatives en cours dominent")
        void ecart() {
            repondreAvec(
                    initiative(1L, "100000.00", 40, "En cours", it6),
                    initiative(2L, "200000.00", 100, "Terminée", it6));

            BudgetGlobalResponse r = service.global(aucunFiltre);

            // effectif 200 000 (seule la Terminée est close) − consommé
            // 240 000 (40 000 + 200 000) = −40 000 : on a davantage « consommé
            // au prorata » que de dépenses définitivement closes, ce qui est
            // le cas normal d'un portefeuille chargé d'initiatives en cours.
            assertThat(r.ecart()).isEqualByComparingTo("-40000.00");
            // −40 000 / 240 000 = −16,7 %
            assertThat(r.ecartPourcentage()).isEqualByComparingTo("-16.7");
        }

        @Test
        @DisplayName("initiative entièrement close : consommé et effectif coïncident, écart nul")
        void ecartNul() {
            repondreAvec(initiative(1L, "200000.00", 100, "Terminée", it6));

            BudgetGlobalResponse r = service.global(aucunFiltre);

            assertThat(r.effectif()).isEqualByComparingTo("200000.00");
            assertThat(r.consomme()).isEqualByComparingTo("200000.00");
            assertThat(r.ecart()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("écart positif : une initiative annulée a engagé son budget entier alors qu'elle n'était qu'à moitié consommée")
        void ecartPositif() {
            // Cas réel : chantier arrêté à 20 % puis abandonné. Le budget est
            // définitivement perdu (effectif), mais l'avancement ne reflète
            // que la partie réellement réalisée (consommé).
            repondreAvec(initiative(1L, "100000.00", 20, "Annulée", it6));

            BudgetGlobalResponse r = service.global(aucunFiltre);

            assertThat(r.consomme()).isEqualByComparingTo("20000.00");
            assertThat(r.effectif()).isEqualByComparingTo("100000.00");
            assertThat(r.ecart()).isEqualByComparingTo("80000.00");
            assertThat(r.ecartPourcentage()).isEqualByComparingTo("400.0");
        }

        @Test
        @DisplayName("recoupement : budgetTotal = consommé + budgetRestant, à la décimale près")
        void recoupement() {
            repondreAvec(
                    initiative(1L, "100000.00", 33, "En cours", it6),
                    initiative(2L, "200000.00", 100, "Terminée", it6),
                    initiative(3L, "300000.00", 0, "Annulée", it6));

            BudgetGlobalResponse r = service.global(aucunFiltre);

            assertThat(r.consomme().add(r.budgetRestant()))
                    .isEqualByComparingTo(r.budgetTotal());
            assertThat(r.budgetRestant()).isEqualByComparingTo("367000.00");
        }

        @Test
        @DisplayName("portefeuille entièrement à 0 % : pas de taux d'écart, plutôt que « 0 % »")
        void ecartSansConsomme() {
            repondreAvec(initiative(1L, "100000.00", 0, "En cours", it6));

            BudgetGlobalResponse r = service.global(aucunFiltre);

            assertThat(r.consomme()).isEqualByComparingTo("0.00");
            assertThat(r.ecart()).isEqualByComparingTo("0.00");
            // Dire « 0 % d'écart » laisserait croire que tout va bien, alors
            // qu'on ne peut tout simplement pas calculer de taux.
            assertThat(r.ecartPourcentage()).isNull();
        }

        @Test
        @DisplayName("budget non renseigné se comporte comme zéro")
        void budgetAbsent() {
            Initiative sansBudget = Initiative.builder()
                    .idInitiative(1L)
                    .budget(null)
                    .avancement(null)
                    .statut(statut(2L, "En cours"))
                    .entite(it6)
                    .build();
            repondreAvec(sansBudget);

            BudgetGlobalResponse r = service.global(aucunFiltre);

            assertThat(r.budgetTotal()).isEqualByComparingTo("0.00");
            assertThat(r.consomme()).isEqualByComparingTo("0.00");
            assertThat(r.effectif()).isEqualByComparingTo("0.00");
            assertThat(r.ecartPourcentage()).isNull();
        }

        @Test
        @DisplayName("répartition : la somme des groupes est égale au total global")
        void repartitionCohérente() {
            repondreAvec(
                    initiative(1L, "100000.00", 40, "En cours", it6),
                    initiative(2L, "200000.00", 100, "Terminée", directionDigitale));

            BudgetGlobalResponse r = service.global(aucunFiltre);

            BigDecimal sommeParStatut = r.parStatut().stream()
                    .map(BudgetGlobalResponse.Repartition::budget)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(sommeParStatut).isEqualByComparingTo(r.budgetTotal());

            BigDecimal sommeParEntite = r.parEntite().stream()
                    .map(BudgetGlobalResponse.Repartition::budget)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            assertThat(sommeParEntite).isEqualByComparingTo(r.budgetTotal());

            // Rattachement à un objectif : la plupart des initiatives n'en ont
            // pas, elles doivent apparaître et non disparaître.
            assertThat(r.parObjectif()).isNotEmpty();
            assertThat(r.parObjectif().get(0).libelle()).isEqualTo("Non rattaché");
            assertThat(r.parObjectif().get(0).nbInitiatives()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Budget par entité")
    class ParEntite {

        @Test
        @DisplayName("toutes les entités apparaissent, même sans initiative")
        void entitesSansInitiative() {
            repondreAvec(initiative(1L, "100000.00", 50, "En cours", it6));

            List<BudgetEntiteResponse> reponses = service.parEntite(aucunFiltre);

            assertThat(reponses).hasSize(2);

            BudgetEntiteResponse avecInitiatives = reponses.stream()
                    .filter(e -> e.idEntite().equals(1L)).findFirst().orElseThrow();
            assertThat(avecInitiatives.nbInitiatives()).isEqualTo(1);
            assertThat(avecInitiatives.budget()).isEqualByComparingTo("100000.00");
            assertThat(avecInitiatives.consomme()).isEqualByComparingTo("50000.00");
            assertThat(avecInitiatives.tauxConsommation()).isEqualByComparingTo("50.0");

            BudgetEntiteResponse vide = reponses.stream()
                    .filter(e -> e.idEntite().equals(2L)).findFirst().orElseThrow();
            assertThat(vide.nbInitiatives()).isZero();
            assertThat(vide.budget()).isEqualByComparingTo("0.00");
            assertThat(vide.tauxConsommation()).isEqualByComparingTo("0.0");
        }

        @Test
        @DisplayName("taux de consommation : budget nul -> 0 %, pas une division par zéro")
        void tauxSurBudgetNul() {
            repondreAvec(initiative(1L, "0.00", 100, "Terminée", it6));

            List<BudgetEntiteResponse> reponses = service.parEntite(aucunFiltre);

            assertThat(reponses.stream()
                    .filter(e -> e.idEntite().equals(1L)).findFirst().orElseThrow()
                    .tauxConsommation()).isEqualByComparingTo("0.0");
        }
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void repondreAvec(Initiative... initiatives) {
        when(initiativeRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(initiatives));
    }

    private static Entite entite(Long id, String code, String nom) {
        Entite e = new Entite();
        e.setIdEntite(id);
        e.setCode(code);
        e.setNom(nom);
        return e;
    }

    private static StatutInitiative statut(Long id, String libelle) {
        StatutInitiative s = new StatutInitiative();
        s.setIdStatut(id);
        s.setLibelle(libelle);
        return s;
    }

    private static Priorite priorite(Long id, String libelle) {
        Priorite p = new Priorite();
        p.setIdPriorite(id);
        p.setLibelle(libelle);
        return p;
    }

    private static Initiative initiative(Long id, String budget, int avancement,
                                         String libelleStatut, Entite entite) {
        return Initiative.builder()
                .idInitiative(id)
                .budget(new BigDecimal(budget))
                .avancement(avancement)
                .archive(false)
                .entite(entite)
                .priorite(priorite(1L, "Haute"))
                .statut(statut(1L, libelleStatut))
                .build();
    }
}
