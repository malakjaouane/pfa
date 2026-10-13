package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.Entite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntiteRepository extends JpaRepository<Entite, Long> {

    List<Entite> findAllByActifTrueOrderByNomAsc();

    List<Entite> findAllByOrderByIdEntiteAsc();
}