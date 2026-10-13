/**
 * Fichier de données unique des sujets de la rubrique publique « Découvrir ».
 *
 * Chaque sujet décrit une page /decouvrir/:sujet : son identifiant d'URL,
 * sa capture d'écran locale et l'emplacement du texte (les libellés restent
 * dans i18n/locales/decouvrir.js, namespace « decouvrir.<id>.* »).
 *
 * Pour AJOUTER un sujet :
 *   1. ajouter une entrée dans SUJETS ci-dessous (id + image) ;
 *   2. ajouter les clés decouvrir.<id>.titre / .intro / .p1 / .p2 / .p3
 *      dans frontend/src/i18n/locales/decouvrir.js (fr + en minimum) ;
 *   3. déposer la capture dans frontend/public/images/screens/ (optionnel :
 *      sans capture, la page affiche un cadre de remplacement soigné).
 * L'ordre d'affichage et le « sujet suivant » suivent le tableau.
 */

/**
 * Captures d'écran réelles de l'application (dossier frontend/public/images).
 * `image: null` = aucune capture pour l'instant : la page affiche un cadre
 * de remplacement au lieu d'une image cassée.
 */
export const SUJETS = [
  {
    id: 'dashboard',
    image: '/images/screens/dashboard.webp',
    alt: 'landing.apercu.dashboard.alt'
  },
  {
    id: 'initiatives',
    image: '/images/screens/kanban.webp',
    alt: 'landing.apercu.kanban.alt'
  },
  {
    id: 'etapes-jalons',
    image: '/images/screens/fiche.webp',
    alt: 'landing.apercu.fiche.alt'
  },
  {
    // Fiche initiative : avancement calculé + liste des étapes datées.
    id: 'avancement',
    image: '/images/screens/avancement.webp',
    alt: 'decouvrir.avancement.alt'
  },
  {
    // Tableau de bord : rangée d'indicateurs avec l'encart Budget total.
    id: 'budget',
    image: '/images/screens/budget.webp',
    alt: 'decouvrir.budget.alt'
  },
  {
    // Tableau de bord : filtre « En cours » appliqué, KPI recalculés.
    id: 'filtres',
    image: '/images/screens/filtres.webp',
    alt: 'decouvrir.filtres.alt'
  },
  {
    // Administration : grille des rôles et de leurs permissions.
    id: 'roles-securite',
    image: '/images/screens/roles-securite.webp',
    alt: 'decouvrir.roles-securite.alt'
  }
];

/** Retourne le sujet dont l'identifiant d'URL correspond, sinon null. */
export function trouverSujet(id) {
  return SUJETS.find((s) => s.id === id) ?? null;
}

/** Sujet situé juste après celui fourni (boucle sur la liste). */
export function sujetSuivant(id) {
  const index = SUJETS.findIndex((s) => s.id === id);
  if (index === -1) return null;
  return SUJETS[(index + 1) % SUJETS.length];
}
