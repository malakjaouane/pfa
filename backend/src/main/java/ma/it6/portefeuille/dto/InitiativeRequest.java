package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Corps de création / modification d'une initiative.
 */
public record InitiativeRequest(
        @NotBlank(message = "Le code est obligatoire") String code,
        @NotBlank(message = "Le nom est obligatoire") String nom,
        String description,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        @DecimalMin(value = "0.0", message = "Le budget doit être >= 0") BigDecimal budget,
        @Min(value = 0, message = "L'avancement doit être entre 0 et 100")
        @Max(value = 100, message = "L'avancement doit être entre 0 et 100") Integer avancement,
        @NotNull(message = "L'entité est obligatoire") Long idEntite,
        Long idResponsable,
        @NotNull(message = "Le statut est obligatoire") Long idStatut,
        @NotNull(message = "La priorité est obligatoire") Long idPriorite,
        Long idObjectif
) {
}