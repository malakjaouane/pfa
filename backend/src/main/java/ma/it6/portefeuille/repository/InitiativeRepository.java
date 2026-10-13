package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.Initiative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InitiativeRepository extends JpaRepository<Initiative, Long>, JpaSpecificationExecutor<Initiative> {

    Optional<Initiative> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdInitiativeNot(String code, Long idInitiative);

    @Query("""
            select distinct i from Initiative i
            left join fetch i.entite
            left join fetch i.responsable
            left join fetch i.statut
            left join fetch i.priorite
            """)
    List<Initiative> findAllFetch();

    Optional<Initiative> findByIdInitiative(Long idInitiative);

    /**
     * Initiatives potentiellement en retard : non archivées, date de fin
     * prévue dépassée, statut != Terminée et avancement &lt; 100 %.
     */
    @Query("""
            select distinct i from Initiative i
            left join fetch i.entite
            left join fetch i.responsable
            left join fetch i.statut
            left join fetch i.priorite
            where i.archive = false
              and i.dateFinPrevue is not null
              and i.dateFinPrevue < :today
              and i.statut.libelle <> 'Terminée'
              and i.avancement < 100
            order by i.dateFinPrevue asc
            """)
    List<Initiative> findEnRetard(@Param("today") LocalDate today);
}
