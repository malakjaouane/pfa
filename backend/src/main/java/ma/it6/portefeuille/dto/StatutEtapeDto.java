package ma.it6.portefeuille.dto;

public record StatutEtapeDto(
        Long idStatutEtape,
        String libelle,
        Integer ordre
) {
}