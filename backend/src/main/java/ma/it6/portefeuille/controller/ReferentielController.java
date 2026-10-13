package ma.it6.portefeuille.controller;

import ma.it6.portefeuille.dto.*;
import ma.it6.portefeuille.service.ReferentialService;
import ma.it6.portefeuille.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Référentiels et gestion des rôles / permissions.
 */
@RestController
@RequestMapping("/api")
public class ReferentielController {

    private final ReferentialService referentialService;
    private final RoleService roleService;

    public ReferentielController(ReferentialService referentialService, RoleService roleService) {
        this.referentialService = referentialService;
        this.roleService = roleService;
    }

    @GetMapping("/referentiels/statuts-initiative")
    public ResponseEntity<List<StatutInitiativeDto>> statutsInitiative() {
        return ResponseEntity.ok(referentialService.statutsInitiative());
    }

    @GetMapping("/referentiels/priorites")
    public ResponseEntity<List<PrioriteDto>> priorites() {
        return ResponseEntity.ok(referentialService.priorites());
    }

    @GetMapping("/referentiels/statuts-etape")
    public ResponseEntity<List<StatutEtapeDto>> statutsEtape() {
        return ResponseEntity.ok(referentialService.statutsEtape());
    }

    @GetMapping("/referentiels/statuts-jalon")
    public ResponseEntity<List<StatutJalonDto>> statutsJalon() {
        return ResponseEntity.ok(referentialService.statutsJalon());
    }

    @GetMapping("/referentiels/statuts-demande")
    public ResponseEntity<List<StatutDemandeDto>> statutsDemande() {
        return ResponseEntity.ok(referentialService.statutsDemande());
    }

    @GetMapping("/referentiels/entites")
    public ResponseEntity<List<EntiteDto>> entites() {
        return ResponseEntity.ok(referentialService.entites());
    }

    @GetMapping("/referentiels/responsables")
    public ResponseEntity<List<UserSummaryDto>> responsables() {
        return ResponseEntity.ok(referentialService.responsablesPotentiels());
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionDto>> permissions() {
        return ResponseEntity.ok(referentialService.permissions());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleDto>> roles() {
        return ResponseEntity.ok(referentialService.roles());
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleDetailDto> roleAvecPermissions(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.roleAvecPermissions(id));
    }

    @PutMapping("/roles/{id}/permissions")
    public ResponseEntity<RoleDetailDto> mettreAJourPermissions(@PathVariable Long id,
                                                                @RequestBody UpdateRolePermissionsRequest request) {
        return ResponseEntity.ok(roleService.mettreAJourPermissions(id, request));
    }
}