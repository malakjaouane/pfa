package ma.it6.portefeuille.dto;

public record PermissionDto(
        Long idPermission,
        String code,
        String libelle
) {
}