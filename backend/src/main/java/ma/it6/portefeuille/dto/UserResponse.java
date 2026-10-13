package ma.it6.portefeuille.dto;

/**
 * Utilisateur renvoyé par l'API (le mot de passe n'est jamais exposé).
 */
public record UserResponse(
        Long idUser,
        String nom,
        String prenom,
        String email,
        String telephone,
        Boolean actif,
        RoleDto role,
        EntiteDto entite
) {
}