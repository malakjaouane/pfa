import { useI18n } from './I18nContext';

/**
 * Les libellés de statuts/priorités proviennent de la base de données en
 * français. On les fait correspondre à des clés de traduction pour les
 * afficher dans la langue choisie.
 */
const CLES_STATUT = {
  'À planifier': 'common.statut.aPlanifier',
  'En cours': 'common.statut.enCours',
  'En pause': 'common.statut.enPause',
  Terminée: 'common.statut.terminee',
  Annulée: 'common.statut.annulee'
};

const CLES_PRIORITE = {
  Basse: 'common.priorite.basse',
  Moyenne: 'common.priorite.moyenne',
  Haute: 'common.priorite.haute',
  Critique: 'common.priorite.critique'
};

const CLES_STATUT_ETAPE = {
  'À faire': 'common.statutEtape.aFaire',
  'En cours': 'common.statutEtape.enCours',
  Terminée: 'common.statutEtape.terminee'
};

const CLES_STATUT_JALON = {
  'À atteindre': 'common.statutJalon.aAtteindre',
  Atteint: 'common.statutJalon.atteint'
};

const CLES_ROLE = {
  ADMIN: 'common.role.ADMIN',
  RESPONSABLE: 'common.role.RESPONSABLE',
  VISITEUR: 'common.role.VISITEUR'
};

/** Traductions des libellés métier issus du référentiel. */
export function useReferentiels() {
  const { t } = useI18n();
  const traduire = (cles, libelle) =>
    libelle && cles[libelle] ? t(cles[libelle]) : libelle;

  return {
    statut: (libelle) => traduire(CLES_STATUT, libelle),
    priorite: (libelle) => traduire(CLES_PRIORITE, libelle),
    statutEtape: (libelle) => traduire(CLES_STATUT_ETAPE, libelle),
    statutJalon: (libelle) => traduire(CLES_STATUT_JALON, libelle),
    /** Rôle par code (ADMIN, RESPONSABLE, VISITEUR). */
    role: (code, libelle) => (code && CLES_ROLE[code] ? t(CLES_ROLE[code]) : libelle),
    /** Permission par code (ex. INITIATIVE_CREER). */
    permission: (code, libelle) =>
      code && t(`common.permission.${code}`) !== `common.permission.${code}`
        ? t(`common.permission.${code}`)
        : libelle
  };
}
