package ma.it6.portefeuille.dto;

import java.math.BigDecimal;

/**
 * Budget d'une entité (unité organisationnelle).
 *
 * @param tauxConsommation part du budget déjà consommé, en pourcentage
 *                         (0 à 100), arrondie à une décimale
 */
public record BudgetEntiteResponse(
        Long idEntite,
        String code,
        String nom,
        Integer nbInitiatives,
        BigDecimal budget,
        BigDecimal consomme,
        BigDecimal effectif,
        BigDecimal ecart,
        BigDecimal tauxConsommation
) {
}
