package ma.it6.portefeuille.security;

import ma.it6.portefeuille.entity.Utilisateur;
import ma.it6.portefeuille.exception.ResourceNotFoundException;
import ma.it6.portefeuille.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Chargement des utilisateurs (avec leurs permissions) pour Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmailWithPermissions(email.toLowerCase().trim())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
        return new PrincipalUser(utilisateur);
    }

    @Transactional(readOnly = true)
    public PrincipalUser chargerParEmail(String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmailWithPermissions(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));
        return new PrincipalUser(utilisateur);
    }
}