package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatutDemandeRepository extends JpaRepository<StatutDemande, Long> {

    Optional<StatutDemande> findByLibelle(String libelle);

    List<StatutDemande> findAllByOrderByOrdreAsc();
}