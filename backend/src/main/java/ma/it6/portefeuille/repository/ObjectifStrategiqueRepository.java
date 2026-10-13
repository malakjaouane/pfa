package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.ObjectifStrategique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObjectifStrategiqueRepository extends JpaRepository<ObjectifStrategique, Long> {

    List<ObjectifStrategique> findAllByOrderByIdObjectifAsc();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdObjectifNot(String code, Long idObjectif);
}