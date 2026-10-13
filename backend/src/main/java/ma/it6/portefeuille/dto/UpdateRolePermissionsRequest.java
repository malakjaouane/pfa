package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Mise à jour des permissions d'un rôle (gestion des rôles).
 */
public record UpdateRolePermissionsRequest(
        @NotNull(message = "La liste des permissions est obligatoire")
        List<Long> idsPermissions
) {
}