package ma.it6.portefeuille.security;

import ma.it6.portefeuille.exception.BadRequestException;

/**
 * Politique de mot de passe appliquée à la création et à la modification d'un
 * compte (et rappelée côté front dans les messages d'erreur).
 *
 * <p>Règles volontairement simples à expliquer devant un jury :</p>
 * <ul>
 *   <li>8 à 64 caractères ;</li>
 *   <li>au moins une lettre et au moins un chiffre ;</li>
 *   <li>différent de l'adresse e-mail (et de sa partie locale).</li>
 * </ul>
 */
public final class PolitiqueMotDePasse {

    public static final int LONGUEUR_MINIME = 8;
    public static final int LONGUEUR_MAXIMALE = 64;

    /** Message réutilisé côté front pour l'affichage de la règle. */
    public static final String ENONCE =
            "Au moins 8 caractères, une lettre et un chiffre, et différent de l'email.";

    private PolitiqueMotDePasse() {
    }

    /**
     * Valide le mot de passe ou lève une {@link BadRequestException} (400).
     *
     * @param email compte destinataire, pour interdire le mot de passe « email »
     */
    public static void valider(String motDePasse, String email) {
        if (motDePasse == null || motDePasse.isBlank()) {
            throw new BadRequestException("Le mot de passe est obligatoire. " + ENONCE);
        }
        String mdp = motDePasse.trim();

        if (mdp.length() < LONGUEUR_MINIME) {
            throw new BadRequestException(
                    "Le mot de passe doit contenir au moins " + LONGUEUR_MINIME + " caractères. " + ENONCE);
        }
        if (mdp.length() > LONGUEUR_MAXIMALE) {
            throw new BadRequestException(
                    "Le mot de passe ne peut pas dépasser " + LONGUEUR_MAXIMALE + " caractères.");
        }
        if (!mdp.matches(".*[A-Za-zÀ-ÿ].*") || !mdp.matches(".*\\d.*")) {
            throw new BadRequestException("Le mot de passe doit contenir au moins une lettre et un chiffre. " + ENONCE);
        }
        if (email != null && !email.isBlank()) {
            String adresse = email.trim().toLowerCase();
            if (mdp.equalsIgnoreCase(adresse)) {
                throw new BadRequestException("Le mot de passe ne peut pas être votre adresse email.");
            }
            int at = adresse.indexOf('@');
            String locale = at > 0 ? adresse.substring(0, at) : adresse;
            if (mdp.equalsIgnoreCase(locale)) {
                throw new BadRequestException("Le mot de passe ne peut pas être votre identifiant email.");
            }
        }
    }
}
