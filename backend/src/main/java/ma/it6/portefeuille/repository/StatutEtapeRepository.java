package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.StatutEtape;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatutEtapeRepository extends JpaRepository<StatutEtape, Long> {

    List<StatutEtape> findAllByOrderByOrdreAsc();
}