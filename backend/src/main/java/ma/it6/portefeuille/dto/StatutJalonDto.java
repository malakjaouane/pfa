package ma.it6.portefeuille.dto;

public record StatutJalonDto(
        Long idStatutJalon,
        String libelle,
        Integer ordre
) {
}