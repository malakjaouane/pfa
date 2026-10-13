package ma.it6.portefeuille.security;

import ma.it6.portefeuille.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Recharge l'utilisateur <strong>à chaque requête</strong> depuis la base.
 *
 * <p>Le jeton JWT seul ne fait pas foi : un compte désactivé (ou un rôle dont
 * les permissions ont changé) doit être refusé immédiatement, et non à
 * l'expiration du jeton (8 h). Le cache court évite toutefois d'interroger la
 * base à chaque appel : une entrée vieillit au maximum
 * {@code app.security.cache-session-ms} millisecondes, et toute modification
 * explicite (changement de rôle, d'état, de permissions) vide l'entrée
 * concernée.</p>
 */
@Service
public class SessionsService {

    /** Entrée de cache : résultat du chargement + horodatage d'expiration. */
    private record Entree(Optional<PrincipalUser> utilisateur, long expire) {
    }

    private final CustomUserDetailsService userDetailsService;
    private final long cacheMs;
    private final Map<String, Entree> cache = new ConcurrentHashMap<>();

    public SessionsService(CustomUserDetailsService userDetailsService,
                           @Value("${app.security.cache-session-ms:30000}") long cacheMs) {
        this.userDetailsService = userDetailsService;
        this.cacheMs = cacheMs;
    }

    /**
     * Retourne l'utilisateur <strong>actif</strong> correspondant à l'e-mail.
     *
     * @return {@link Optional#empty()} si le compte n'existe pas ou est
     *         désactivé : l'appelant laisse alors le contexte de sécurité vide
     *         et le EntryPoint renvoie 401.
     */
    public Optional<PrincipalUser> chargerActif(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        String cle = email.toLowerCase().trim();
        long maintenant = System.currentTimeMillis();
        Entree entree = cache.get(cle);

        if (entree == null || entree.expire() <= maintenant) {
            Optional<PrincipalUser> charge;
            try {
                charge = Optional.of(userDetailsService.chargerParEmail(cle));
            } catch (ResourceNotFoundException introuvable) {
                charge = Optional.empty();
            }
            // Un compte désactivé n'est plus authentifié, même si son jeton
            // est encore techniquement valide.
            charge = charge.filter(u -> Boolean.TRUE.equals(u.getUtilisateur().getActif()));
            entree = new Entree(charge, maintenant + cacheMs);
            cache.put(cle, entree);
        }
        return entree.utilisateur();
    }

    /** Invalide le cache d'un utilisateur (rôle, e-mail, état actif, …). */
    public void invalider(String email) {
        if (email != null && !email.isBlank()) {
            cache.remove(email.toLowerCase().trim());
        }
    }

    /** Vide le cache : les permissions de tous les rôles ont pu changer. */
    public void invaliderTout() {
        cache.clear();
    }

    /**
     * Invalide l'entrée d'un utilisateur <strong>après le commit</strong> de la
     * transaction en cours. Invalider avant le commit ferait relire la base
     * trop tôt et remettrait en cache l'ancienne valeur.
     */
    public void invaliderApresCommit(String email) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    invalider(email);
                }
            });
        } else {
            invalider(email);
        }
    }

    /** {@link #invaliderTout()} différé jusqu'au commit de la transaction. */
    public void invaliderToutApresCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cache.clear();
                }
            });
        } else {
            cache.clear();
        }
    }
}
