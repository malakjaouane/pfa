import { useEffect, useState } from 'react';
import { Link, Navigate, useParams } from 'react-router-dom';
import { useI18n } from '../i18n/I18nContext';
import LanguageSwitcher from '../components/LanguageSwitcher';
import { trouverSujet, sujetSuivant } from '../data/decouvrir';
import { IconArrowRight, IconCheck, IconLayers } from '../components/Icons';

/* Les 3 points clés d'un sujet (clés decouvrir.<id>.p1/p2/p3). */
const POINTS = ['p1', 'p2', 'p3'];

/**
 * Page publique « Découvrir » : explique un sujet de la vitrine avec une
 * capture de l'application, sans connexion.
 *
 * - `/decouvrir/<sujet inconnu>` redirige vers l'accueil ;
 * - le retour en haut de page est assuré à chaque changement de sujet ;
 * - sans capture (image null ou erreur de chargement), un cadre de
 *   remplacement soigné est affiché au lieu d'une image cassée.
 */
export default function DecouvrirPage() {
  const { sujet } = useParams();
  const { t } = useI18n();
  const info = trouverSujet(sujet);
  const suivant = sujetSuivant(sujet);

  // Capture en échec (fichier retiré, erreur réseau) : repli sur le cadre.
  const [imageErreur, setImageErreur] = useState(false);

  // Remonter en haut de la page à chaque changement de sujet.
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' });
    setImageErreur(false);
  }, [sujet]);

  // Sujet inconnu : retour à l'accueil.
  if (!info) return <Navigate to="/" replace />;

  const cible = `decouvrir.${info.id}`;
  const titre = t(`${cible}.titre`);
  const titreSuivant = t(`decouvrir.${suivant.id}.titre`);

  return (
    <div className="sn-page">
      {/* En-tête : marque, sélecteur de langue, retour à l'accueil */}
      <header className="sn-nav sn-decouvrir-nav">
        <div className="sn-nav-inner">
          <Link to="/" className="sn-logo">
            <img
              className="sn-logo-img"
              src="/images/logo-it6.png"
              alt="IT6 Academy"
              width="132"
              height="138"
            />
            <span className="sn-logo-text">
              <strong>IT6 Consulting</strong>
              <small>{t('landing.brand.sousTitre')}</small>
            </span>
          </Link>
          <div className="sn-nav-actions">
            <LanguageSwitcher variante="vitrine" />
            <Link to="/" className="sn-btn sn-btn-outline sn-btn-sm">
              {t('decouvrir.retour')}
            </Link>
          </div>
        </div>
      </header>

      <main className="sn-decouvrir">
        <div className="sn-decouvrir-tete">
          <span className="sn-eyebrow sn-eyebrow-dark">{t('decouvrir.eyebrow')}</span>
          <h1>{titre}</h1>
          <p className="sn-decouvrir-intro">{t(`${cible}.intro`)}</p>
        </div>

        {/* Grande image : capture réelle, sinon cadre de remplacement */}
        <figure className="sn-decouvrir-figure">
          {info.image && !imageErreur ? (
            <img src={info.image} alt={t(info.alt)} loading="lazy" onError={() => setImageErreur(true)} />
          ) : (
            <div className="sn-decouvrir-placeholder" role="img" aria-label={t(info.alt)}>
              <span className="sn-decouvrir-placeholder-ic" aria-hidden="true">
                <IconLayers size={26} />
              </span>
              <strong>{t('decouvrir.imageManquante.titre')}</strong>
              <p>{t('decouvrir.imageManquante.texte')}</p>
            </div>
          )}
        </figure>

        {/* 3 points clés */}
        <section className="sn-decouvrir-points" aria-label={t('decouvrir.pointsCles')}>
          <h2>{t('decouvrir.pointsCles')}</h2>
          <ul>
            {POINTS.map((p) => (
              <li key={p}>
                <span className="sn-check" aria-hidden="true">
                  <IconCheck size={12} />
                </span>
                {t(`${cible}.${p}`)}
              </li>
            ))}
          </ul>
        </section>

        {/* Accès à l'application + sujet suivant */}
        <div className="sn-decouvrir-actions">
          <Link to="/login" className="sn-btn sn-btn-green sn-btn-lg">
            {t('decouvrir.accesApp')}
            <IconArrowRight size={18} />
          </Link>
          <Link to={`/decouvrir/${suivant.id}`} className="sn-btn sn-btn-outline sn-btn-lg">
            {t('decouvrir.suivant')} : {titreSuivant}
            <IconArrowRight size={18} />
          </Link>
        </div>
      </main>

      <footer className="sn-footer sn-decouvrir-footer">
        <div className="sn-footer-bottom">
          {t('landing.footer.copyright', { annee: new Date().getFullYear() })}
        </div>
      </footer>
    </div>
  );
}
