package ma.it6.portefeuille.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Vérifie, <strong>avant le démarrage</strong>, que les secrets ne sont pas
 * ceux livrés par défaut.
 *
 * <p>Les valeurs par défaut d'application.yml ne servent qu'au lancement local
 * : dès qu'un profil autre que {@code dev} / {@code test} / {@code local} est
 * actif (recette, production, conteneur…), leur utilisation fait échouer le
 * démarrage avec un message explicite, au lieu de laisser une application
 * exposée avec une clé de signature et un mot de passe de base de données
 * connus de tous.</p>
 *
 * @see #PROFILS_DEVELOPPEMENT profils autorisés à conserver les défauts
 */
@Component
public class VerificationSecrets implements InitializingBean {

    /** Profils pour lesquels les valeurs par défaut restent tolérées. */
    private static final String[] PROFILS_DEVELOPPEMENT = {"dev", "test", "local"};

    /** Clé de signature JWT livrée dans application.yml (usage local uniquement). */
    private static final String SECRET_JWT_DEFAUT =
            "PfaIT6ConsultingAgdalRabat2026CleSecreteTresLonguePourHS256===";

    /** Mot de passe de la base de données livré dans application.yml. */
    private static final String MOT_DE_PASSE_BDD_DEFAUT = "pfa2026";

    /** Longueur minimale d'une clé de signature HS256 (octets). */
    private static final int TAILLE_MINIME_SECRET = 32;

    private static final Logger LOG = LoggerFactory.getLogger(VerificationSecrets.class);

    private final Environment environment;
    private final String secretJwt;
    private final String motDePasseBdd;

    public VerificationSecrets(Environment environment,
                               @Value("${app.jwt.secret}") String secretJwt,
                               @Value("${spring.datasource.password}") String motDePasseBdd) {
        this.environment = environment;
        this.secretJwt = secretJwt;
        this.motDePasseBdd = motDePasseBdd;
    }

    @Override
    public void afterPropertiesSet() {
        List<String> problemes = verifier();

        if (enDeveloppement()) {
            if (!problemes.isEmpty()) {
                LOG.warn("Secrets par défaut utilisés (profil de développement) : {}", problemes);
            }
            return;
        }
        if (!problemes.isEmpty()) {
            String[] profils = environment.getActiveProfiles();
            String affiche = profils.length == 0 ? "(par défaut)" : String.join(", ", profils);
            throw new IllegalStateException("""
                    Démarrage refusé : configuration de sécurité non renforcée (profil actif : %s).
                      - %s
                    Définissez les variables d'environnement correspondantes
                    (voir .env.example) : JWT_SECRET, DB_PASSWORD, DB_URL, DB_USERNAME.
                    """.formatted(affiche, String.join("\n  - ", problemes)));
        }
    }

    /** @return la liste des problèmes détectés (vide si la configuration est saine). */
    private List<String> verifier() {
        List<String> problemes = new ArrayList<>();

        if (secretJwt == null || secretJwt.isBlank()) {
            problemes.add("app.jwt.secret (JWT_SECRET) est vide.");
        } else {
            if (SECRET_JWT_DEFAUT.equals(secretJwt)) {
                problemes.add("app.jwt.secret (JWT_SECRET) conserve la valeur par défaut connue.");
            } else if (secretJwt.length() < TAILLE_MINIME_SECRET) {
                problemes.add("app.jwt.secret (JWT_SECRET) fait moins de "
                        + TAILLE_MINIME_SECRET + " caractères : signature HS256 insuffisante.");
            }
        }

        if (motDePasseBdd == null || motDePasseBdd.isBlank()) {
            problemes.add("spring.datasource.password (DB_PASSWORD) est vide.");
        } else if (MOT_DE_PASSE_BDD_DEFAUT.equals(motDePasseBdd)) {
            problemes.add("spring.datasource.password (DB_PASSWORD) conserve la valeur par défaut connue.");
        }

        return problemes;
    }

    private boolean enDeveloppement() {
        // Sans profil explicite, Spring ne remplit pas getActiveProfiles() :
        // on retombe sur les profils par défaut (« dev » déclaré dans
        // application.yml).
        String[] profils = environment.getActiveProfiles();
        if (profils.length == 0) {
            profils = environment.getDefaultProfiles();
        }
        for (String attendu : PROFILS_DEVELOPPEMENT) {
            for (String actif : profils) {
                if (attendu.equalsIgnoreCase(actif)) {
                    return true;
                }
            }
        }
        return false;
    }
}
