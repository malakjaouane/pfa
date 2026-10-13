package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.Etape;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtapeRepository extends JpaRepository<Etape, Long> {

    List<Etape> findByInitiativeIdInitiativeOrderByOrdreAsc(Long idInitiative);
}