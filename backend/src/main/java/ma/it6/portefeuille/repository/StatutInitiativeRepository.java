package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.StatutInitiative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatutInitiativeRepository extends JpaRepository<StatutInitiative, Long> {

    Optional<StatutInitiative> findByLibelle(String libelle);

    List<StatutInitiative> findAllByOrderByOrdreAsc();
}