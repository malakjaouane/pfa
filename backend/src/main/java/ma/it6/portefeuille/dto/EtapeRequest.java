package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Corps de création / modification d'une étape.
 */
public record EtapeRequest(
        @NotBlank(message = "Le nom de l'étape est obligatoire") String nom,
        String description,
        Integer ordre,
        LocalDate dateDebut,
        LocalDate dateFinPrevue,
        @Min(value = 0, message = "L'avancement doit être entre 0 et 100")
        @Max(value = 100, message = "L'avancement doit être entre 0 et 100") Integer avancement,
        Long idStatutEtape
) {
}