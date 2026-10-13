package ma.it6.portefeuille.service;

import ma.it6.portefeuille.dto.UserRequest;
import ma.it6.portefeuille.dto.UserResponse;
import ma.it6.portefeuille.entity.Entite;
import ma.it6.portefeuille.entity.Role;
import ma.it6.portefeuille.entity.Utilisateur;
import ma.it6.portefeuille.exception.BadRequestException;
import ma.it6.portefeuille.exception.ResourceNotFoundException;
import ma.it6.portefeuille.mapper.UtilisateurMapper;
import ma.it6.portefeuille.repository.EntiteRepository;
import ma.it6.portefeuille.repository.RoleRepository;
import ma.it6.portefeuille.repository.UtilisateurRepository;
import ma.it6.portefeuille.security.PolitiqueMotDePasse;
import ma.it6.portefeuille.security.SessionsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des utilisateurs (profil ADMINISTRATEUR).
 * Les mots de passe sont systématiquement stockés hashés (BCrypt).
 *
 * <p>Toute modification invalide la session en cache : le compte désactivé ou le
 * rôle évolué est pris en compte à la <strong>requête suivante</strong>, sans
 * attendre l'expiration du jeton.</p>
 */
@Service
public class UtilisateurService {

    /** Code du rôle administrateur (voir {@code DataInitializer}). */
    private static final String ROLE_ADMIN = "ADMIN";

    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final EntiteRepository entiteRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionsService sessionsService;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              RoleRepository roleRepository,
                              EntiteRepository entiteRepository,
                              PasswordEncoder passwordEncoder,
                              SessionsService sessionsService) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.entiteRepository = entiteRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionsService = sessionsService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> lister() {
        return utilisateurRepository.findAllByOrderByIdUserAsc().stream()
                .map(UtilisateurMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse trouver(Long idUser) {
        return UtilisateurMapper.toResponse(getUtilisateur(idUser));
    }

    @Transactional
    public UserResponse creer(UserRequest request) {
        if (utilisateurRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new BadRequestException("Un utilisateur avec cet email existe déjà.");
        }
        if (request.motDePasse() == null || request.motDePasse().isBlank()) {
            throw new BadRequestException("Le mot de passe est obligatoire à la création. "
                    + PolitiqueMotDePasse.ENONCE);
        }
        PolitiqueMotDePasse.valider(request.motDePasse(), request.email());

        Role role = roleRepository.findById(request.idRole())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable : " + request.idRole()));

        Utilisateur u = Utilisateur.builder()
                .nom(request.nom().trim())
                .prenom(request.prenom().trim())
                .email(request.email().trim().toLowerCase())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .telephone(request.telephone())
                .actif(request.actif() == null || request.actif())
                .role(role)
                .entite(resoudreEntite(request.idEntite()))
                .build();

        return UtilisateurMapper.toResponse(utilisateurRepository.save(u));
    }

    @Transactional
    public UserResponse modifier(Long idUser, UserRequest request) {
        Utilisateur u = getUtilisateur(idUser);
        String ancienEmail = u.getEmail();

        String nouvelEmail = request.email().trim().toLowerCase();
        if (utilisateurRepository.existsByEmailAndIdUserNot(nouvelEmail, idUser)) {
            throw new BadRequestException("Un autre utilisateur possède déjà cet email.");
        }

        Role role = roleRepository.findById(request.idRole())
                .orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable : " + request.idRole()));
        boolean actifApres = request.actif() == null ? Boolean.TRUE.equals(u.getActif()) : request.actif();

        // Garde-fou : jamais sans administrateur actif (RM : continuité d'exploitation).
        verifierDernierAdministrateur(u, role, actifApres);

        u.setNom(request.nom().trim());
        u.setPrenom(request.prenom().trim());
        u.setEmail(nouvelEmail);
        u.setTelephone(request.telephone());
        u.setActif(actifApres);
        u.setRole(role);
        u.setEntite(resoudreEntite(request.idEntite()));

        // Mot de passe optionnel à la modification : s'il est fourni, il est
        // validé par la politique puis ré-hashé.
        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            PolitiqueMotDePasse.valider(request.motDePasse(), nouvelEmail);
            u.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        }

        Utilisateur enregistre = utilisateurRepository.save(u);

        // Le compte est immédiatement ré-authentifié à la requête suivante
        // (invalidation différée jusqu'au commit pour ne pas relire trop tôt).
        sessionsService.invaliderApresCommit(ancienEmail);
        sessionsService.invaliderApresCommit(enregistre.getEmail());

        return UtilisateurMapper.toResponse(enregistre);
    }

    /**
     * Changement de mot de passe par l'utilisateur connecté (profil courant).
     *
     * <p>Contrôles : mot de passe actuel vérifié par hash, politique de
     * mot de passe appliquée, et nouveau mot de passe différent de l'actuel.
     * La session en cache est invalidée pour que le hash courant soit relu.</p>
     */
    @Transactional
    public void changerMotDePassePropre(String email, String motDePasseActuel, String nouveauMotDePasse) {
        String adresse = email == null ? "" : email.trim().toLowerCase();
        Utilisateur u = utilisateurRepository.findByEmail(adresse)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + adresse));

        if (u.getMotDePasse() == null || !passwordEncoder.matches(motDePasseActuel, u.getMotDePasse())) {
            throw new BadRequestException("Le mot de passe actuel est incorrect.");
        }
        PolitiqueMotDePasse.valider(nouveauMotDePasse, u.getEmail());
        if (passwordEncoder.matches(nouveauMotDePasse, u.getMotDePasse())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'actuel.");
        }

        u.setMotDePasse(passwordEncoder.encode(nouveauMotDePasse));
        utilisateurRepository.save(u);
        sessionsService.invaliderApresCommit(u.getEmail());
    }

    /**
     * Refuse une modification qui laisserait le portefeuille sans aucun
     * compte administrateur actif : on ne peut ni désactiver ni rétrograder
     * le dernier administrateur en exercice.
     */
    private void verifierDernierAdministrateur(Utilisateur u, Role roleApres, boolean actifApres) {
        boolean actifAvant = Boolean.TRUE.equals(u.getActif());
        boolean adminAvant = u.getRole() != null && ROLE_ADMIN.equals(u.getRole().getCode());
        boolean adminApres = ROLE_ADMIN.equals(roleApres.getCode());

        if (!adminAvant || !actifAvant) {
            return; // ce compte n'est pas (ou plus) un administrateur actif
        }
        if (adminApres && actifApres) {
            return; // il le reste
        }
        if (!utilisateurRepository.existsByIdUserNotAndActifTrueAndRoleCode(u.getIdUser(), ROLE_ADMIN)) {
            throw new BadRequestException(
                    "Opération refusée : ce compte est le dernier administrateur actif. "
                            + "Désactivez d'abord un autre administrateur.");
        }
    }

    private Utilisateur getUtilisateur(Long idUser) {
        return utilisateurRepository.findById(idUser)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + idUser));
    }

    private Entite resoudreEntite(Long idEntite) {
        if (idEntite == null) {
            return null;
        }
        return entiteRepository.findById(idEntite)
                .orElseThrow(() -> new ResourceNotFoundException("Entité introuvable : " + idEntite));
    }
}
