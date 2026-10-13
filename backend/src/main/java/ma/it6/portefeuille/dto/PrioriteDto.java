package ma.it6.portefeuille.dto;

public record PrioriteDto(
        Long idPriorite,
        String libelle,
        Integer ordre
) {
}