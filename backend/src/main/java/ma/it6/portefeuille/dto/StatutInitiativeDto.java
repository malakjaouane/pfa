package ma.it6.portefeuille.dto;

public record StatutInitiativeDto(
        Long idStatut,
        String libelle,
        Integer ordre
) {
}