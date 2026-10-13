package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.DemandeInitiative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DemandeInitiativeRepository extends JpaRepository<DemandeInitiative, Long> {

    @Query("""
            select distinct d from DemandeInitiative d
            left join fetch d.entite
            left join fetch d.demandeur
            left join fetch d.priorite
            left join fetch d.statut
            left join fetch d.initiativeLiee
            order by d.dateSoumission desc, d.idDemande desc
            """)
    List<DemandeInitiative> findAllFetch();
}