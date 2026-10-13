package ma.it6.portefeuille.dto;

import java.util.List;

/**
 * Rôle avec la liste de ses permissions (gestion des rôles).
 */
public record RoleDetailDto(
        Long idRole,
        String code,
        String libelle,
        List<PermissionDto> permissions
) {
}