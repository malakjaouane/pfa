package ma.it6.portefeuille.security;

import ma.it6.portefeuille.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitation du nombre de tentatives de connexion (anti force brute).
 *
 * <p>Deux compteurs glissants, indépendants :</p>
 * <ul>
 *   <li><strong>par compte</strong> — empêche d'épuiser le dictionnaire sur un
 *       seul e-mail ;</li>
 *   <li><strong>par adresse IP</strong> — empêche d'essayer plusieurs comptes
 *       depuis une même source.</li>
 * </ul>
 *
 * <p>Une connexion réussie remet le compteur du compte à zéro. Les compteurs ne
 * sont pas persistants : ils valent pour la durée de vie du processus, ce qui
 * suffit à freiner une attaque tout en restant inoffensif pour l'application.</p>
 */
@Service
public class ProtectionConnexions {

    private static final String INCONNU = "-";

    /** Horodatages des échecs, par clé (e-mail ou IP), les plus récents à droite. */
    private final Map<String, Deque<Long>> echecsParCle = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> echecsParIp = new ConcurrentHashMap<>();

    private final long fenetreMs;
    private final int maxParCompte;
    private final int maxParAdresse;

    public ProtectionConnexions(
            @Value("${app.securite.tentatives.max-compte:5}") int maxParCompte,
            @Value("${app.securite.tentatives.max-ip:20}") int maxParAdresse,
            @Value("${app.securite.tentatives.fenetre-ms:900000}") long fenetreMs) {
        this.maxParCompte = maxParCompte;
        this.maxParAdresse = maxParAdresse;
        this.fenetreMs = fenetreMs;
    }

    /**
     * Lève {@link TooManyRequestsException} (429) si la fenêtre de temps est
     * saturée pour le compte ou pour l'adresse.
     */
    public void verifierAutorise(String adresseIp, String email) {
        String cle = cleCompte(email);
        if (tropDeTentatives(echecsParCle.get(cle), maxParCompte)
                || tropDeTentatives(echecsParIp.get(adresseIp), maxParAdresse)) {
            throw new TooManyRequestsException(
                    "Trop de tentatives de connexion. Réessayez dans quelques minutes.");
        }
    }

    /** Note un échec d'authentification (identifiants incorrects). */
    public void enregistrerEchec(String adresseIp, String email) {
        long maintenant = System.currentTimeMillis();
        noter(echecsParCle, cleCompte(email), maintenant);
        noter(echecsParIp, adresseIp, maintenant);
    }

    /** Une connexion réussie remet le compteur du compte à zéro. */
    public void reussite(String adresseIp, String email) {
        echecsParCle.remove(cleCompte(email));
        echecsParIp.remove(adresseIp);
    }

    // ------------------------------------------------------------------
    // Internes
    // ------------------------------------------------------------------

    private static String cleCompte(String email) {
        return email == null || email.isBlank() ? INCONNU : email.trim().toLowerCase();
    }

    private void noter(Map<String, Deque<Long>> compteurs, String cle, long maintenant) {
        Deque<Long> fenetre = compteurs.computeIfAbsent(cle, k -> new ArrayDeque<>());
        synchronized (fenetre) {
            fenetre.addLast(maintenant);
            purger(fenetre, maintenant, fenetreMs);
        }
    }

    private boolean tropDeTentatives(Deque<Long> fenetre, int maximum) {
        if (fenetre == null) {
            return false;
        }
        synchronized (fenetre) {
            purger(fenetre, System.currentTimeMillis(), fenetreMs);
            return fenetre.size() >= maximum;
        }
    }

    /** Retire les échecs plus vieux que la fenêtre glissante. */
    private static void purger(Deque<Long> fenetre, long maintenant, long fenetreMs) {
        long limite = maintenant - fenetreMs;
        while (!fenetre.isEmpty() && fenetre.peekFirst() < limite) {
            fenetre.removeFirst();
        }
    }
}
