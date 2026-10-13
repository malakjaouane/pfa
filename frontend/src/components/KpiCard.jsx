/**
 * Carte KPI du dashboard.
 * `icone` peut être un nœud React (ex. <IconProject />) ou un simple texte.
 * `aide` est un texte d'explication affiché au survol (attribut `title`) :
 * il sert notamment à préciser la nature d'un montant estimé.
 */
export default function KpiCard({ libelle, valeur, unite = '', color = 'blue', icone = null, aide = null }) {
  return (
    <div className={`kpi-card kpi-${color}`} title={aide ?? undefined}>
      {icone && <div className="kpi-icone">{icone}</div>}
      <div className="kpi-contenu">
        <span className="kpi-valeur">{valeur}{unite}</span>
        <span className="kpi-libelle">{libelle}</span>
      </div>
    </div>
  );
}
