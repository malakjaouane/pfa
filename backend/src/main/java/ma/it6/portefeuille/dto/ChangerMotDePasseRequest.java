package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Changement de mot de passe par l'utilisateur connecté lui-même
 * (POST /api/auth/changer-mot-de-passe).
 *
 * <p>L'administrateur qui crée ou modifie un compte reste géré par
 * {@code UserRequest} ; ici il s'agit du profil « je change mon propre
 * mot de passe », d'où l'exigence du mot de passe actuel.</p>
 */
public record ChangerMotDePasseRequest(
        @NotBlank(message = "Le mot de passe actuel est obligatoire")
        String motDePasseActuel,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        String nouveauMotDePasse
) {
}
