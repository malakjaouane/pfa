package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    List<Permission> findAllByOrderByIdPermissionAsc();
}