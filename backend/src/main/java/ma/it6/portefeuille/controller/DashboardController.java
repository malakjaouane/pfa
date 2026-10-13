package ma.it6.portefeuille.controller;

import ma.it6.portefeuille.dto.DashboardResponse;
import ma.it6.portefeuille.dto.InitiativeFilterRequest;
import ma.it6.portefeuille.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard : KPI de pilotage du portefeuille.
 *
 * <p>Accepte les <strong>mêmes filtres</strong> que la liste des initiatives
 * (statut, entité, responsable, priorité, objectif, période « du … au … »,
 * recherche) : tous les indicateurs et tous les graphiques suivent alors la
 * sélection affichée.</p>
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> dashboard(@ModelAttribute InitiativeFilterRequest filtre) {
        return ResponseEntity.ok(dashboardService.calculer(filtre));
    }
}
