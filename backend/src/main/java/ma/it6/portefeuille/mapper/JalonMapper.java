package ma.it6.portefeuille.mapper;

import ma.it6.portefeuille.dto.JalonResponse;
import ma.it6.portefeuille.dto.StatutJalonDto;
import ma.it6.portefeuille.entity.Jalon;
import ma.it6.portefeuille.entity.StatutJalon;
import ma.it6.portefeuille.service.JalonService;

import java.time.LocalDate;

/**
 * Conversions entre l'entité Jalon et son DTO.
 */
public final class JalonMapper {

    private JalonMapper() {
    }

    public static StatutJalonDto toStatutDto(StatutJalon s) {
        if (s == null) {
            return null;
        }
        return new StatutJalonDto(s.getIdStatutJalon(), s.getLibelle(), s.getOrdre());
    }

    public static JalonResponse toResponse(Jalon j) {
        if (j == null) {
            return null;
        }
        return new JalonResponse(
                j.getIdJalon(),
                j.getNom(),
                j.getDescription(),
                j.getDatePrevue(),
                j.getDateRealisation(),
                toStatutDto(j.getStatutJalon()),
                j.getInitiative() != null ? j.getInitiative().getIdInitiative() : null,
                enRetard(j, LocalDate.now())
        );
    }

    /**
     * Jalon non atteint dont la date prévue est déjà passée (le même calcul
     * que pour les initiatives, appliqué au niveau de l'étape).
     */
    public static boolean enRetard(Jalon j, LocalDate aujourdHui) {
        if (j.getDatePrevue() == null || j.getDatePrevue().isAfter(aujourdHui)) {
            return false;
        }
        String statut = j.getStatutJalon() == null ? null : j.getStatutJalon().getLibelle();
        return !JalonService.STATUT_ATTEINT.equals(statut);
    }
}
