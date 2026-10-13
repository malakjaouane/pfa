package ma.it6.portefeuille.dto;

public record EntiteDto(
        Long idEntite,
        String code,
        String nom,
        Boolean actif
) {
}