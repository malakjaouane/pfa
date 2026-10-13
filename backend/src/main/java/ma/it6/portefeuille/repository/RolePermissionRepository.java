package ma.it6.portefeuille.repository;

import ma.it6.portefeuille.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRoleIdRoleOrderByPermissionCodeAsc(Long idRole);

    void deleteByRoleIdRole(Long idRole);
}