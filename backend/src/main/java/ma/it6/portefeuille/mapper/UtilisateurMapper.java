package ma.it6.portefeuille.mapper;

import ma.it6.portefeuille.dto.EntiteDto;
import ma.it6.portefeuille.dto.RoleDto;
import ma.it6.portefeuille.dto.UserResponse;
import ma.it6.portefeuille.dto.UserSummaryDto;
import ma.it6.portefeuille.entity.Entite;
import ma.it6.portefeuille.entity.Role;
import ma.it6.portefeuille.entity.Utilisateur;

/**
 * Conversions entre l'entité Utilisateur et ses DTO.
 */
public final class UtilisateurMapper {

    private UtilisateurMapper() {
    }

    public static RoleDto toRoleDto(Role role) {
        if (role == null) {
            return null;
        }
        return new RoleDto(role.getIdRole(), role.getCode(), role.getLibelle());
    }

    public static EntiteDto toEntiteDto(Entite entite) {
        if (entite == null) {
            return null;
        }
        return new EntiteDto(entite.getIdEntite(), entite.getCode(), entite.getNom(), entite.getActif());
    }

    public static UserResponse toResponse(Utilisateur u) {
        if (u == null) {
            return null;
        }
        return new UserResponse(
                u.getIdUser(),
                u.getNom(),
                u.getPrenom(),
                u.getEmail(),
                u.getTelephone(),
                u.getActif(),
                toRoleDto(u.getRole()),
                toEntiteDto(u.getEntite())
        );
    }

    public static UserSummaryDto toSummary(Utilisateur u) {
        if (u == null) {
            return null;
        }
        return new UserSummaryDto(
                u.getIdUser(),
                (u.getPrenom() == null ? "" : u.getPrenom()) + " " + (u.getNom() == null ? "" : u.getNom()),
                u.getEmail()
        );
    }
}