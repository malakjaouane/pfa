/**
 * Barre de progression d'avancement (0-100 %).
 */
export default function ProgressBar({ valeur, large = false }) {
  const v = Math.max(0, Math.min(100, valeur ?? 0));
  const classe = v >= 100 ? 'progress-vert' : v >= 50 ? 'progress-bleu' : 'progress-ambre';
  return (
    <div className={`progress ${large ? 'progress-large' : ''}`}>
      <div className={`progress-fill ${classe}`} style={{ width: `${v}%` }}>
        {large && `${v} %`}
      </div>
    </div>
  );
}