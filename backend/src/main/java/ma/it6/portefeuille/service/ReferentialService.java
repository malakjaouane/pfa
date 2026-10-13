package ma.it6.portefeuille.service;

import ma.it6.portefeuille.dto.*;
import ma.it6.portefeuille.entity.*;
import ma.it6.portefeuille.exception.ResourceNotFoundException;
import ma.it6.portefeuille.mapper.EtapeMapper;
import ma.it6.portefeuille.mapper.InitiativeMapper;
import ma.it6.portefeuille.mapper.JalonMapper;
import ma.it6.portefeuille.mapper.UtilisateurMapper;
import ma.it6.portefeuille.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Référentiels métier : statuts, priorités, entités, profils, statuts
 * d'étape et de jalon. Les listes alimentent les listes déroulantes
 * de l'interface.
 */
@Service
@Transactional(readOnly = true)
public class ReferentialService {

    private final StatutInitiativeRepository statutInitiativeRepository;
    private final PrioriteRepository prioriteRepository;
    private final EntiteRepository entiteRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final StatutEtapeRepository statutEtapeRepository;
    private final StatutJalonRepository statutJalonRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final StatutDemandeRepository statutDemandeRepository;

    public ReferentialService(StatutInitiativeRepository statutInitiativeRepository,
                              PrioriteRepository prioriteRepository,
                              EntiteRepository entiteRepository,
                              RoleRepository roleRepository,
                              PermissionRepository permissionRepository,
                              StatutEtapeRepository statutEtapeRepository,
                              StatutJalonRepository statutJalonRepository,
                              UtilisateurRepository utilisateurRepository,
                              StatutDemandeRepository statutDemandeRepository) {
        this.statutInitiativeRepository = statutInitiativeRepository;
        this.prioriteRepository = prioriteRepository;
        this.entiteRepository = entiteRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.statutEtapeRepository = statutEtapeRepository;
        this.statutJalonRepository = statutJalonRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.statutDemandeRepository = statutDemandeRepository;
    }

    public List<StatutInitiativeDto> statutsInitiative() {
        return statutInitiativeRepository.findAllByOrderByOrdreAsc().stream()
                .map(s -> new StatutInitiativeDto(s.getIdStatut(), s.getLibelle(), s.getOrdre()))
                .toList();
    }

    public List<PrioriteDto> priorites() {
        return prioriteRepository.findAllByOrderByOrdreAsc().stream()
                .map(p -> new PrioriteDto(p.getIdPriorite(), p.getLibelle(), p.getOrdre()))
                .toList();
    }

    public List<EntiteDto> entites() {
        return entiteRepository.findAllByOrderByIdEntiteAsc().stream()
                .map(UtilisateurMapper::toEntiteDto)
                .toList();
    }

    public List<RoleDto> roles() {
        return roleRepository.findAllByOrderByIdRoleAsc().stream()
                .map(r -> new RoleDto(r.getIdRole(), r.getCode(), r.getLibelle()))
                .toList();
    }

    public List<PermissionDto> permissions() {
        return permissionRepository.findAllByOrderByIdPermissionAsc().stream()
                .map(p -> new PermissionDto(p.getIdPermission(), p.getCode(), p.getLibelle()))
                .toList();
    }

    public List<StatutEtapeDto> statutsEtape() {
        return statutEtapeRepository.findAllByOrderByOrdreAsc().stream()
                .map(EtapeMapper::toStatutDto)
                .toList();
    }

    public List<StatutJalonDto> statutsJalon() {
        return statutJalonRepository.findAllByOrderByOrdreAsc().stream()
                .map(JalonMapper::toStatutDto)
                .toList();
    }

    public List<StatutDemandeDto> statutsDemande() {
        return statutDemandeRepository.findAllByOrderByOrdreAsc().stream()
                .map(s -> new StatutDemandeDto(s.getIdStatutDemande(), s.getLibelle(), s.getOrdre()))
                .toList();
    }

    /**
     * Responsables potentiels : utilisateurs actifs (pour l'affectation
     * d'un responsable à une initiative).
     */
    public List<UserSummaryDto> responsablesPotentiels() {
        return utilisateurRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActif()))
                .map(UtilisateurMapper::toSummary)
                .toList();
    }

    public StatutInitiative getStatutParLibelleOuDefaut(String libelle) {
        return statutInitiativeRepository.findByLibelle(libelle)
                .orElseThrow(() -> new ResourceNotFoundException("Statut d'initiative introuvable : " + libelle));
    }
}