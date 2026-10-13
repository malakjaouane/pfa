package ma.it6.portefeuille.dto;

/**
 * Corps du refus d'une demande (motif facultatif mais recommandé).
 */
public record RefusRequest(
        String motifRefus
) {
}