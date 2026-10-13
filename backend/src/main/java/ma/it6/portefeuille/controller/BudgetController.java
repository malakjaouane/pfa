package ma.it6.portefeuille.controller;

import ma.it6.portefeuille.dto.BudgetEntiteResponse;
import ma.it6.portefeuille.dto.BudgetGlobalResponse;
import ma.it6.portefeuille.dto.InitiativeFilterRequest;
import ma.it6.portefeuille.service.BudgetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST des agrégats budgétaires.
 *
 * <p>Les trois points d'entrée acceptent <strong>exactement les mêmes
 * filtres</strong> que la liste des initiatives et le dashboard : le budget
 * affiché correspond toujours à la sélection visible à l'écran.</p>
 */
@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /** Vue globale : totaux, écarts et réparutions (statut, entité, objectif, priorité). */
    @GetMapping
    public BudgetGlobalResponse global(@ModelAttribute InitiativeFilterRequest filtre) {
        return budgetService.global(filtre);
    }

    /** Budget détaillé par entité de rattachement. */
    @GetMapping("/entites")
    public List<BudgetEntiteResponse> parEntite(@ModelAttribute InitiativeFilterRequest filtre) {
        return budgetService.parEntite(filtre);
    }

    /**
     * Répartition par objectif stratégique (alignement initiative → objectif).
     * Exposée séparément pour que la vue d'alignement reste utilisable sans
     * télécharger la vue globale complète.
     */
    @GetMapping("/objectifs")
    public List<BudgetGlobalResponse.Repartition> parObjectif(@ModelAttribute InitiativeFilterRequest filtre) {
        return budgetService.global(filtre).parObjectif();
    }
}
