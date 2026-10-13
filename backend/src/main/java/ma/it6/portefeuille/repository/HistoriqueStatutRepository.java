package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {

    /**
     * Journal d'une initiative, du plus récent au plus ancien : c'est l'ordre
     * dans lequel la fiche détaillée l'affiche.
     */
    List<HistoriqueStatut> findByInitiativeIdInitiativeOrderByDateChangementDesc(Long idInitiative);
}
