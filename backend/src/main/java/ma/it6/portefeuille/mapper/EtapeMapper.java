package ma.it6.portefeuille.mapper;

import ma.it6.portefeuille.dto.EtapeResponse;
import ma.it6.portefeuille.dto.StatutEtapeDto;
import ma.it6.portefeuille.entity.Etape;
import ma.it6.portefeuille.entity.StatutEtape;

/**
 * Conversions entre l'entité Etape et son DTO.
 */
public final class EtapeMapper {

    private EtapeMapper() {
    }

    public static StatutEtapeDto toStatutDto(StatutEtape s) {
        if (s == null) {
            return null;
        }
        return new StatutEtapeDto(s.getIdStatutEtape(), s.getLibelle(), s.getOrdre());
    }

    public static EtapeResponse toResponse(Etape e) {
        if (e == null) {
            return null;
        }
        return new EtapeResponse(
                e.getIdEtape(),
                e.getNom(),
                e.getDescription(),
                e.getOrdre(),
                e.getDateDebut(),
                e.getDateFinPrevue(),
                e.getAvancement(),
                toStatutDto(e.getStatutEtape()),
                e.getInitiative() != null ? e.getInitiative().getIdInitiative() : null
        );
    }
}