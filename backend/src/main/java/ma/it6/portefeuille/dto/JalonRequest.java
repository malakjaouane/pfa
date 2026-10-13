package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * Corps de création / modification d'un jalon.
 * La date de réalisation peut rester vide tant que le jalon n'est pas
 * atteint (RM15).
 */
public record JalonRequest(
        @NotBlank(message = "Le nom du jalon est obligatoire") String nom,
        String description,
        LocalDate datePrevue,
        LocalDate dateRealisation,
        Long idStatutJalon
) {
}