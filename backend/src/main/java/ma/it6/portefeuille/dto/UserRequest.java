package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "Le prénom est obligatoire") String prenom,
        @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
        String motDePasse,
        String telephone,
        Boolean actif,
        @jakarta.validation.constraints.NotNull(message = "Le rôle est obligatoire") Long idRole,
        Long idEntite
) {
}