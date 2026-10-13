import { useReferentiels } from '../i18n/referentiels';

/**
 * Badge coloré selon le statut métier d'une initiative.
 * Le libellé (issu de la base, en français) est traduit dans la langue active.
 */
const STYLES = {
  'À planifier': 'badge-gris',
  'En cours': 'badge-bleu',
  'En pause': 'badge-ambre',
  'Terminée': 'badge-vert',
  'Annulée': 'badge-rouge'
};

export default function StatutBadge({ libelle }) {
  const ref = useReferentiels();
  return <span className={`badge ${STYLES[libelle] ?? 'badge-gris'}`}>{ref.statut(libelle)}</span>;
}
