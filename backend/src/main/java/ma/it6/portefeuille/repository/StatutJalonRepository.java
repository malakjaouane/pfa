package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.StatutJalon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatutJalonRepository extends JpaRepository<StatutJalon, Long> {

    List<StatutJalon> findAllByOrderByOrdreAsc();
}