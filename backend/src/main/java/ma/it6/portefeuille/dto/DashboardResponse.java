package ma.it6.portefeuille.dto;

import java.util.List;

/**
 * Données du dashboard et des KPI.
 */
public record DashboardResponse(
        long totalInitiatives,
        long enCours,
        long terminees,
        long enPause,
        long enRetard,
        long jalonsEnRetard,
        java.math.BigDecimal budgetTotal,
        double avancementMoyen,
        List<ChartDatum> parStatut,
        List<ChartDatum> parPriorite,
        List<ChartDatum> parEntite,
        List<ChartDatum> parResponsable,
        List<ChartDatum> parObjectif,
        List<InitiativeResponse> initiativesEnRetard
) {

    public record ChartDatum(String libelle, long count) {
    }
}
