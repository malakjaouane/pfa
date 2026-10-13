import { useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import StatutBadge from './StatutBadge';
import ProgressBar from './ProgressBar';
import { useI18n } from '../i18n/I18nContext';
import { useReferentiels } from '../i18n/referentiels';
import { useAuth } from '../AuthContext';
import api, { messageErreur } from '../api';
import { IconAlert, IconTarget } from './Icons';

/**
 * Vue Kanban des initiatives, groupées par statut (ordre du référentiel).
 * Complément visuel de la vue tableau : chaque carte ouvre la fiche.
 *
 * <p><b>Glisser-déposer</b> : déplacer une carte dans une autre colonne
 * déclenche {@code PUT /initiatives/{id}/statut}, qui applique côté serveur
 * les mêmes règles de cycle de vie que la modification par formulaire. Le
 * serveur refuse les transitions interdites (400) et le motif est affiché
 * sous forme de bandeau — on ne laisse jamais croire à l'utilisateur qu'un
 * glisser a été pris en compte alors qu'il a été refusé.</p>
 *
 * <p>Le déplacement n'est proposé qu'à ceux qui ont la permission
 * {@code INITIATIVE_MODIFIER} : un Visiteur garde un Kanban purement
 * consultatif, et ne voit même pas apparaître l'affordance du glisser.</p>
 */
export default function KanbanBoard({ initiatives, statuts, onDeplacee }) {
  const { t } = useI18n();
  const ref = useReferentiels();
  const navigate = useNavigate();
  const { hasPermission } = useAuth();

  const peutDeplacer = hasPermission('INITIATIVE_MODIFIER');

  const [colonneSurvol, setColonneSurvol] = useState(null);
  const [enCours, setEnCours] = useState(null);
  const [erreur, setErreur] = useState(null);

  /**
   * Id de la carte en cours de glissement.
   *
   * <p>Ref et non state : cette valeur n'a pas besoin de déclencher un
   * rendu — elle sert uniquement à distinguer, dans {@code onClick}, un vrai
   * clic d'une fin de glisser-déposer. Le navigateur déclenche en effet un
   * clic après le drop, ce qui ferait ouvrir la fiche juste après avoir
   * déplacé la carte.</p>
   */
  const idGlisse = useRef(null);

  const deplacer = async (idInitiative, statutCible) => {
    const initiative = initiatives.find((i) => i.idInitiative === idInitiative);
    if (!initiative || initiative.statut?.idStatut === statutCible.idStatut) {
      return;
    }
    setEnCours(idInitiative);
    setErreur(null);
    try {
      await api.put(`/initiatives/${idInitiative}/statut`, {
        idStatut: statutCible.idStatut,
        commentaire: null
      });
      await onDeplacee?.();
    } catch (err) {
      setErreur(err.response?.data?.message ?? t('common.kanban.deplacementRefuse'));
    } finally {
      setEnCours(null);
    }
  };

  if (initiatives.length === 0) {
    return <p className="texte-muet">{t('common.kanban.aucunResultat')}</p>;
  }

  const colonnes = statuts.map((statut) => ({
    statut,
    items: initiatives.filter((i) => i.statut?.idStatut === statut.idStatut)
  }));

  return (
    <>
      {peutDeplacer && (
        <p className="texte-muet kanban-aide">{t('common.kanban.glisserDeplacer')}</p>
      )}

      {erreur && (
        <div role="alert" className="alerte alerte-erreur kanban-erreur">
          <span>{erreur}</span>
          <button type="button" onClick={() => setErreur(null)}>
            {t('common.kanban.fermer')}
          </button>
        </div>
      )}

      <div className="kanban">
        {colonnes.map(({ statut, items }) => {
          const survolee = colonneSurvol === statut.idStatut;
          return (
            <div
              key={statut.idStatut}
              className={`kanban-col${survolee ? ' colonne-survol' : ''}`}
              onDragOver={(e) => {
                if (!peutDeplacer) return;
                // Sans preventDefault, le navigateur refuse le drop et la
                // carte rebondit dans sa colonne d'origine.
                e.preventDefault();
                e.dataTransfer.dropEffect = 'move';
                setColonneSurvol(statut.idStatut);
              }}
              onDragLeave={(e) => {
                // Les colonnes enfants (les cartes) déclenchent aussi
                // dragLeave : on ne quitte la colonne que si on en sort
                // réellement.
                if (e.currentTarget.contains(e.relatedTarget)) return;
                setColonneSurvol((c) => (c === statut.idStatut ? null : c));
              }}
              onDrop={(e) => {
                e.preventDefault();
                setColonneSurvol(null);
                const id = Number(e.dataTransfer.getData('text/plain'));
                if (Number.isFinite(id)) deplacer(id, statut);
              }}
            >
              <div className="kanban-entete">
                <StatutBadge libelle={statut.libelle} />
                <span className="kanban-compte">{items.length}</span>
              </div>
              <div className="kanban-cartes">
                {items.length === 0 && <p className="texte-muet kanban-vide">—</p>}
                {items.map((i) => {
                  const deplacee = enCours === i.idInitiative;
                  return (
                    <div
                      key={i.idInitiative}
                      className={`kanban-carte${i.enRetard ? ' carte-retard' : ''}${peutDeplacer ? ' carte-deplacable' : ''}${deplacee ? ' carte-en-deplacement' : ''}`}
                      draggable={peutDeplacer && !deplacee}
                      onDragStart={(e) => {
                        idGlisse.current = i.idInitiative;
                        e.dataTransfer.setData('text/plain', String(i.idInitiative));
                        e.dataTransfer.effectAllowed = 'move';
                      }}
                      onDragEnd={() => {
                        idGlisse.current = null;
                        setColonneSurvol(null);
                      }}
                      onClick={() => {
                        // Après un glisser, le navigateur émet un clic :
                        // on ne navigue que s'il n'y a pas eu de déplacement.
                        if (idGlisse.current === null) navigate(`/initiatives/${i.idInitiative}`);
                      }}
                      title={
                        i.enRetard
                          ? t('common.kanban.enRetard')
                          : t('common.kanban.ouvert', {
                              description: i.description ?? t('common.kanban.voirFiche')
                            })
                      }
                    >
                      <div className="kanban-carte-entete">
                        <span className="texte-muet kanban-code">{i.code}</span>
                        {i.enRetard && <span className="icone-alerte" title={t('common.kanban.enRetard')}><IconAlert size={15} /></span>}
                      </div>
                      {/* Le nom reste un lien : clic droit, ouverture dans un
                          onglet, lecture par un lecteur d'écran. */}
                      <Link
                        to={`/initiatives/${i.idInitiative}`}
                        className="kanban-nom"
                        onClick={(e) => e.stopPropagation()}
                      >
                        {i.nom}
                      </Link>
                      <div className="kanban-meta">
                        <span className="badge badge-gris">{i.priorite?.libelle ? ref.priorite(i.priorite.libelle) : '—'}</span>
                        {i.objectif && <span className="badge badge-violet"><IconTarget size={12} /> {i.objectif.libelle}</span>}
                      </div>
                      <div className="kanban-dates">
                        <span>{i.dateDebut ?? '—'} → {i.dateFinPrevue ?? '—'}</span>
                        <span>{i.responsable?.nomComplet ?? t('common.nonAffecte')}</span>
                      </div>
                      <ProgressBar valeur={i.avancement} />
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}
