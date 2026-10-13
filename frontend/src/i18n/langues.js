/**
 * Langues proposées par l'application.
 *
 * Pour AJOUTER UNE LANGUE :
 *   1. ajouter une entrée ici (code ISO, libellé, rtl) ;
 *   2. fournir ses traductions sous la même clé de code dans chaque
 *      fichier de `src/i18n/locales/*.js`.
 * Aucun autre changement n'est nécessaire : le sélecteur de langue et le
 * sens d'écriture (LTR/RTL) s'adaptent automatiquement.
 */
export const LANGUES = [
  { code: 'fr', libelle: 'Français', rtl: false },
  { code: 'en', libelle: 'English', rtl: false },
  { code: 'ar', libelle: 'العربية', rtl: true },
  { code: 'es', libelle: 'Español', rtl: false }
];

export const LANGUE_DEFAUT = 'fr';

export const CODES_LANGUES = LANGUES.map((l) => l.code);
