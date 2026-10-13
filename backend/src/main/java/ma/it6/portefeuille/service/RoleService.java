package ma.it6.portefeuille.service;

import ma.it6.portefeuille.dto.*;
import ma.it6.portefeuille.entity.Role;
import ma.it6.portefeuille.entity.RolePermission;
import ma.it6.portefeuille.entity.RolePermissionId;
import ma.it6.portefeuille.exception.BadRequestException;
import ma.it6.portefeuille.exception.ResourceNotFoundException;
import ma.it6.portefeuille.mapper.UtilisateurMapper;
import ma.it6.portefeuille.repository.PermissionRepository;
import ma.it6.portefeuille.repository.RolePermissionRepository;
import ma.it6.portefeuille.repository.RoleRepository;
import ma.it6.portefeuille.security.SessionsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des rôles et de leurs permissions (profil ADMINISTRATEUR).
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final SessionsService sessionsService;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       RolePermissionRepository rolePermissionRepository,
                       SessionsService sessionsService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.sessionsService = sessionsService;
    }

    @Transactional(readOnly = true)
    public List<RoleDetailDto> rolesAvecPermissions() {
        return roleRepository.findAllByOrderByIdRoleAsc().stream()
                .map(this::toDetail)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoleDetailDto roleAvecPermissions(Long idRole) {
        Role role = roleRepository.findById(idRole)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable : " + idRole));
        return toDetail(role);
    }

    /**
     * Remplace l'ensemble des permissions affectées à un rôle.
     * Il n'est pas possible de retirer toutes les permissions à un rôle
     * ou de modifier le rôle d'un administrateur de façon incompatible.
     */
    @Transactional
    public RoleDetailDto mettreAJourPermissions(Long idRole, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findById(idRole)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable : " + idRole));

        List<Long> ids = request.idsPermissions();
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("Un rôle doit posséder au moins une permission.");
        }

        rolePermissionRepository.deleteByRoleIdRole(idRole);

        for (Long idPermission : ids) {
            var permission = permissionRepository.findById(idPermission)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission introuvable : " + idPermission));
            RolePermission rp = new RolePermission();
            rp.setId(new RolePermissionId(idRole, idPermission));
            rp.setRole(role);
            rp.setPermission(permission);
            rolePermissionRepository.save(rp);
        }

        // Les permissions de tous les comptes portant ce rôle changent :
        // on vide le cache de sessions (différé jusqu'au commit).
        sessionsService.invaliderToutApresCommit();

        return roleAvecPermissions(idRole);
    }

    private RoleDetailDto toDetail(Role role) {
        List<PermissionDto> permissions = rolePermissionRepository.findByRoleIdRoleOrderByPermissionCodeAsc(role.getIdRole())
                .stream()
                .map(rp -> new PermissionDto(rp.getPermission().getIdPermission(),
                        rp.getPermission().getCode(), rp.getPermission().getLibelle()))
                .toList();
        return new RoleDetailDto(role.getIdRole(), role.getCode(), role.getLibelle(), permissions);
    }
}