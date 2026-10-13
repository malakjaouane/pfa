import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import { useI18n } from '../i18n/I18nContext';
import LanguageSwitcher from '../components/LanguageSwitcher';
import {
  IconDashboard,
  IconKanban,
  IconRoadmap,
  IconInbox,
  IconTarget,
  IconShield,
  IconArrowRight,
  IconArrowUp,
  IconCheck,
  IconClock,
  IconLayers,
  IconSearch,
  IconMenu,
  IconClose
} from '../components/Icons';

/* ------------------------------------------------------------------ */
/* Contenus statiques (libellés fournis par i18n/locales/landing.js)   */
/* ------------------------------------------------------------------ */

const CHIFFRES = [
  { valeur: '06', cleLibelle: 'landing.chiffre.modules' },
  { valeur: '05', cleLibelle: 'landing.chiffre.statuts' },
  { valeur: '03', cleLibelle: 'landing.chiffre.roles' },
  { valeur: '01', cleLibelle: 'landing.chiffre.source' }
];

/** Colonnes de la section « problème / solution ». */
const CONTRASTE = {
  probleme: [
    'landing.contraste.probleme.i1',
    'landing.contraste.probleme.i2',
    'landing.contraste.probleme.i3'
  ],
  solution: [
    'landing.contraste.solution.i1',
    'landing.contraste.solution.i2',
    'landing.contraste.solution.i3'
  ]
};

/**
 * Cartes de fonctionnalités. `sujet` indique la page /decouvrir/:sujet
 * ouverte quand on clique sur la carte.
 */
const FONCTIONNALITES = [
  { Icone: IconSearch, cleTitre: 'landing.fonction.recherche.titre', cleTexte: 'landing.fonction.recherche.texte', sujet: 'initiatives' },
  { Icone: IconKanban, cleTitre: 'landing.fonction.kanban.titre', cleTexte: 'landing.fonction.kanban.texte', sujet: 'initiatives' },
  { Icone: IconRoadmap, cleTitre: 'landing.fonction.roadmap.titre', cleTexte: 'landing.fonction.roadmap.texte', sujet: 'etapes-jalons' },
  { Icone: IconInbox, cleTitre: 'landing.fonction.demandes.titre', cleTexte: 'landing.fonction.demandes.texte', sujet: 'initiatives' },
  { Icone: IconTarget, cleTitre: 'landing.fonction.objectifs.titre', cleTexte: 'landing.fonction.objectifs.texte', sujet: 'dashboard' },
  { Icone: IconShield, cleTitre: 'landing.fonction.roles.titre', cleTexte: 'landing.fonction.roles.texte', sujet: 'roles-securite' },
  { Icone: IconClock, cleTitre: 'landing.fonction.alertes.titre', cleTexte: 'landing.fonction.alertes.texte', sujet: 'etapes-jalons' },
  { Icone: IconLayers, cleTitre: 'landing.fonction.archives.titre', cleTexte: 'landing.fonction.archives.texte', sujet: 'initiatives' }
];

/**
 * Captures d'écran réelles de l'application (dossier public/images/screens).
 * `sujet` : page /decouvrir/:sujet atteinte en cliquant sur la capture.
 */
const APERCU = [
  {
    img: '/images/screens/dashboard.webp',
    cleAlt: 'landing.apercu.dashboard.alt',
    cleTitre: 'landing.apercu.dashboard.titre',
    cleTexte: 'landing.apercu.dashboard.texte',
    sujet: 'dashboard'
  },
  {
    img: '/images/screens/kanban.webp',
    cleAlt: 'landing.apercu.kanban.alt',
    cleTitre: 'landing.apercu.kanban.titre',
    cleTexte: 'landing.apercu.kanban.texte',
    sujet: 'initiatives'
  },
  {
    img: '/images/screens/fiche.webp',
    cleAlt: 'landing.apercu.fiche.alt',
    cleTitre: 'landing.apercu.fiche.titre',
    cleTexte: 'landing.apercu.fiche.texte',
    sujet: 'etapes-jalons'
  }
];

/**
 * Mots importants soulignés en pointillés dans les textes : chaque entrée
 * relie la clé i18n d'un texte à la liste des mots à rendre cliquables
 * (clé i18n du mot → sujet /decouvrir/:sujet).
 */
const LIENS_TEXTES = {
  'landing.hero.lead': [
    { motCle: 'landing.mot.initiatives', sujet: 'initiatives' },
    { motCle: 'landing.mot.roadmap', sujet: 'etapes-jalons' }
  ],
  'landing.contraste.solution.i3': [
    { motCle: 'landing.mot.kpi', sujet: 'dashboard' }
  ],
  'landing.apercu.dashboard.texte': [
    { motCle: 'landing.mot.avancement', sujet: 'avancement' },
    { motCle: 'landing.mot.budget', sujet: 'budget' }
  ],
  'landing.apercu.fiche.texte': [
    { motCle: 'landing.mot.jalons', sujet: 'etapes-jalons' },
    { motCle: 'landing.mot.avancement', sujet: 'avancement' },
    { motCle: 'landing.mot.budget', sujet: 'budget' }
  ],
  'landing.etape.piloter.texte': [
    { motCle: 'landing.mot.avancement', sujet: 'avancement' }
  ]
};

const ETAPES = [
  { n: '01', cleTitre: 'landing.etape.soumettre.titre', cleTexte: 'landing.etape.soumettre.texte' },
  { n: '02', cleTitre: 'landing.etape.evaluer.titre', cleTexte: 'landing.etape.evaluer.texte' },
  { n: '03', cleTitre: 'landing.etape.transformer.titre', cleTexte: 'landing.etape.transformer.texte' },
  { n: '04', cleTitre: 'landing.etape.piloter.titre', cleTexte: 'landing.etape.piloter.texte' }
];

/** Ancres suivies pour le lien « en cours » de la barre de navigation. */
const SECTIONS_NAV = ['probleme', 'fonctionnalites', 'apercu', 'etapes', 'contact'];

/**
 * Durée (ms) de l'animation des chiffres clés, une fois la bande visible.
 * Volontairement courte : la page reste sobre, l'effet est un accent.
 */
const DUREE_COMPTEUR = 900;

/* ------------------------------------------------------------------ */
/* Page                                                                */
/* ------------------------------------------------------------------ */

/**
 * Page d'accueil publique (vitrine), présentée avant la connexion.
 * Sept sections : navigation, hero, chiffres, problème/solution,
 * fonctionnalités, aperçu de l'application, étapes, appel à l'action,
 * puis pied de page.
 *
 * Toutes les images sont locales (frontend/public/images) : la page
 * reste affichée sans connexion Internet.
 */
export default function LandingPage() {
  const { user } = useAuth();
  const { t } = useI18n();
  const [menuOuvert, setMenuOuvert] = useState(false);
  // Effets de défilement : barre compacte, section en cours, retour en haut.
  const [navCompacte, setNavCompacte] = useState(false);
  const [sectionActive, setSectionActive] = useState('');
  const [hautPageVisible, setHautPageVisible] = useState(false);
  // Chiffres clés : valeurs affichées, animées de 0 à la valeur réelle.
  const [compteurs, setCompteurs] = useState(() => CHIFFRES.map((c) => c.valeur));
  const cible = user ? '/dashboard' : '/login';
  const libelleCta = user ? t('landing.cta.ouvrirApp') : t('landing.cta.seConnecter');

  /* Effets de défilement (CSS + IntersectionObserver, sans librairie).
     Progressif : sans JS — ou avec animations réduites demandées — la page
     reste entièrement visible et les chiffres affichent leur valeur finale. */
  useEffect(() => {
    const reduit = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // 1) Apparitions en fondu : la classe posée ici débloque les styles de
    //    masquage ; chaque cible révélée est ensuite suivie une seule fois.
    let obsApparait = null;
    const page = document.querySelector('.sn-page');
    if (!reduit && 'IntersectionObserver' in window) {
      page?.classList.add('sn-defilement');
      obsApparait = new IntersectionObserver((entrees) => {
        entrees.forEach((e) => {
          if (!e.isIntersecting) return;
          e.target.classList.add('est-visible');
          obsApparait.unobserve(e.target);
        });
      }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' });
      document.querySelectorAll('[data-apparait]').forEach((el) => obsApparait.observe(el));
    }

    // 2) Chiffres animés : une seule boucle rAF, lancée quand la bande entre
    //    dans le champ, avec décélération (ease-out) sur ~0,9 s.
    let obsChiffres = null;
    let rafChiffres = 0;
    const bandeau = document.querySelector('.sn-stats');
    if (!reduit && bandeau && 'IntersectionObserver' in window) {
      obsChiffres = new IntersectionObserver((entrees) => {
        if (!entrees.some((e) => e.isIntersecting)) return;
        obsChiffres.disconnect();
        const valeurs = CHIFFRES.map((c) => parseInt(c.valeur, 10));
        const depart = performance.now();
        const pas = (maintenant) => {
          const progression = Math.min(1, (maintenant - depart) / DUREE_COMPTEUR);
          const adouci = 1 - Math.pow(1 - progression, 3);
          setCompteurs(valeurs.map((v) => String(Math.round(v * adouci)).padStart(2, '0')));
          if (progression < 1) rafChiffres = requestAnimationFrame(pas);
        };
        setCompteurs(valeurs.map(() => '00'));
        rafChiffres = requestAnimationFrame(pas);
      }, { threshold: 0.4 });
      obsChiffres.observe(bandeau);
    }

    // 3) Section en cours : bande d'observation fine (milieu d'écran), la
    //    section qui la traverse devient le lien actif de la navigation.
    let obsSections = null;
    if ('IntersectionObserver' in window) {
      obsSections = new IntersectionObserver((entrees) => {
        entrees.forEach((e) => {
          if (e.isIntersecting) setSectionActive(e.target.id);
        });
      }, { rootMargin: '-45% 0px -50% 0px', threshold: 0 });
      SECTIONS_NAV.forEach((id) => {
        const el = document.getElementById(id);
        if (el) obsSections.observe(el);
      });
    }

    // 4) Un seul écouteur de défilement, throttlé via requestAnimationFrame.
    let rafScroll = 0;
    const majScroll = () => {
      rafScroll = 0;
      const y = window.scrollY;
      setNavCompacte(y > 16);
      setHautPageVisible(y > 640);
    };
    const surScroll = () => {
      if (!rafScroll) rafScroll = requestAnimationFrame(majScroll);
    };
    window.addEventListener('scroll', surScroll, { passive: true });
    majScroll();

    return () => {
      window.removeEventListener('scroll', surScroll);
      cancelAnimationFrame(rafScroll);
      cancelAnimationFrame(rafChiffres);
      obsApparait?.disconnect();
      obsChiffres?.disconnect();
      obsSections?.disconnect();
      page?.classList.remove('sn-defilement');
    };
  }, []);

  const allerEnHaut = () => {
    const reduit = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    window.scrollTo({ top: 0, behavior: reduit ? 'auto' : 'smooth' });
  };

  /**
   * Texte traduit avec liens « Découvrir » : entoure chaque mot clé
   * (première occurrence, insensible à la casse) d'un lien vers la page
   * /decouvrir/:sujet correspondant. Sans mot présent, renvoie le texte.
   */
  const echapperMot = (s) => s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const avecLiens = (cleTexte) => {
    const texte = t(cleTexte);
    const mots = (LIENS_TEXTES[cleTexte] ?? [])
      .map((l) => ({ ...l, mot: t(l.motCle) }))
      .filter((l) => l.mot);
    if (!mots.length) return texte;
    const motif = new RegExp(`(${mots.map((l) => echapperMot(l.mot)).join('|')})`, 'gi');
    return texte.split(motif).map((morceau, i) => {
      // split avec un groupe de capture : indices pairs = texte, impairs = mot
      if (i % 2 === 0) return morceau;
      const lien = mots.find((l) => l.mot.toLowerCase() === morceau.toLowerCase());
      if (!lien) return morceau;
      return (
        <Link key={`${lien.sujet}-${i}`} to={`/decouvrir/${lien.sujet}`} className="sn-mot-cle">
          {morceau}
        </Link>
      );
    });
  };

  return (
    <div className="sn-page">
      {/* ---------------------------------------------------------- */}
      {/* 1. Navigation                                              */}
      {/* ---------------------------------------------------------- */}
      <header className={`sn-nav${navCompacte ? ' sn-nav-compacte' : ''}`}>
        <div className="sn-nav-inner">
          <a href="#top" className="sn-logo">
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
          </a>
          <nav className="sn-nav-links">
            <a href="#probleme" className={sectionActive === 'probleme' ? 'est-courant' : undefined}>
              {t('landing.nav.probleme')}
            </a>
            <a href="#fonctionnalites" className={sectionActive === 'fonctionnalites' ? 'est-courant' : undefined}>
              {t('landing.nav.fonctionnalites')}
            </a>
            <a href="#apercu" className={sectionActive === 'apercu' ? 'est-courant' : undefined}>
              {t('landing.nav.apercu')}
            </a>
            <a href="#etapes" className={sectionActive === 'etapes' ? 'est-courant' : undefined}>
              {t('landing.nav.etapes')}
            </a>
            <a href="#contact" className={sectionActive === 'contact' ? 'est-courant' : undefined}>
              {t('landing.nav.contact')}
            </a>
          </nav>
          <div className="sn-nav-actions">
            <LanguageSwitcher variante="vitrine" />
            <Link to={cible} className="sn-btn sn-btn-green sn-btn-sm sn-nav-cta">
              {libelleCta}
              <IconArrowRight size={15} />
            </Link>
            <button
              type="button"
              className="sn-nav-burger"
              onClick={() => setMenuOuvert((v) => !v)}
              aria-label={menuOuvert ? t('landing.menu.fermer') : t('landing.menu.ouvrir')}
              aria-expanded={menuOuvert}
              aria-controls="sn-menu-mobile"
            >
              {menuOuvert ? <IconClose size={22} /> : <IconMenu size={22} />}
            </button>
          </div>
        </div>

        {menuOuvert && (
          <nav className="sn-menu-mobile" id="sn-menu-mobile" aria-label={t('landing.menu.navigationPrincipale')}>
            <a href="#probleme" className={sectionActive === 'probleme' ? 'est-courant' : undefined} onClick={() => setMenuOuvert(false)}>{t('landing.nav.probleme')}</a>
            <a href="#fonctionnalites" className={sectionActive === 'fonctionnalites' ? 'est-courant' : undefined} onClick={() => setMenuOuvert(false)}>{t('landing.nav.fonctionnalites')}</a>
            <a href="#apercu" className={sectionActive === 'apercu' ? 'est-courant' : undefined} onClick={() => setMenuOuvert(false)}>{t('landing.nav.apercu')}</a>
            <a href="#etapes" className={sectionActive === 'etapes' ? 'est-courant' : undefined} onClick={() => setMenuOuvert(false)}>{t('landing.nav.etapes')}</a>
            <a href="#contact" className={sectionActive === 'contact' ? 'est-courant' : undefined} onClick={() => setMenuOuvert(false)}>{t('landing.nav.contact')}</a>
            <Link to={cible} className="sn-menu-cta" onClick={() => setMenuOuvert(false)}>
              {libelleCta}
              <IconArrowRight size={16} />
            </Link>
          </nav>
        )}
      </header>

      {/* ---------------------------------------------------------- */}
      {/* 2. Hero                                                    */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-hero" id="top">
        <span className="sn-hero-glow sn-hero-glow-a" aria-hidden="true" />
        <span className="sn-hero-glow sn-hero-glow-b" aria-hidden="true" />
        <div className="sn-hero-inner">
          <div>
            <span className="sn-eyebrow">
              <span className="sn-eyebrow-dot" />
              {t('landing.hero.eyebrow')}
            </span>
            <h1>
              {t('landing.hero.titre1')}
              <br />
              {t('landing.hero.titre2Prefix')}
              <span className="sn-highlight">{t('landing.hero.titre2Highlight')}</span>
            </h1>
            <p className="sn-hero-lead">{avecLiens('landing.hero.lead')}</p>
            <div className="sn-cta">
              <Link to={cible} className="sn-btn sn-btn-green sn-btn-lg">
                {libelleCta}
                <IconArrowRight size={18} />
              </Link>
            </div>
            <p className="sn-hero-note">{t('landing.accesReserve')}</p>
            <ul className="sn-avantages">
              <li><span className="sn-check"><IconCheck size={12} /></span>{t('landing.avantage.pilotage')}</li>
              <li><span className="sn-check"><IconCheck size={12} /></span>{t('landing.avantage.workflow')}</li>
              <li><span className="sn-check"><IconCheck size={12} /></span>{t('landing.avantage.alignement')}</li>
              <li><span className="sn-check"><IconCheck size={12} /></span>{t('landing.avantage.decisions')}</li>
            </ul>
          </div>

          <div className="sn-hero-visual">
            <span className="sn-shot-glow" aria-hidden="true" />
            <figure className="sn-shot">
              <span className="sn-shot-bar" aria-hidden="true">
                <span className="sn-shot-dot" />
                <span className="sn-shot-dot" />
                <span className="sn-shot-dot" />
                <span className="sn-shot-url">app.it6.ma / dashboard</span>
              </span>
              <img
                src="/images/screens/dashboard.webp"
                alt={t('landing.hero.altCapture')}
                width="1600"
                height="1000"
                loading="eager"
              />
            </figure>
          </div>
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* 3. Chiffres clés                                           */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-stats" aria-label={t('landing.stats.aria')}>
        <div className="sn-stats-inner">
          {CHIFFRES.map((c, i) => (
            <div key={c.valeur} className="sn-stat">
              <span className="sn-stat-valeur">{compteurs[i]}</span>
              <span className="sn-stat-libelle">{t(c.cleLibelle)}</span>
            </div>
          ))}
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* 4. Problème / solution                                     */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-section" id="probleme">
        <div className="sn-section-head" data-apparait>
          <span className="sn-eyebrow sn-eyebrow-dark">{t('landing.contraste.eyebrow')}</span>
          <h2>{t('landing.contraste.titre')}</h2>
          <p>{t('landing.contraste.texte')}</p>
        </div>

        <div className="sn-duo">
          <article className="sn-duo-carte sn-duo-carte-sombre" data-apparait>
            <h3>{t('landing.contraste.probleme.titre')}</h3>
            <ul>
              {CONTRASTE.probleme.map((cle) => (
                <li key={cle}>
                  <span className="sn-duo-marque sn-duo-marque-k-o" aria-hidden="true">×</span>
                  {t(cle)}
                </li>
              ))}
            </ul>
          </article>

          <div className="sn-duo-photo" data-apparait>
            <img
              src="/images/probleme.webp"
              alt={t('landing.contraste.photoAlt')}
              width="1200"
              height="800"
              loading="lazy"
            />
          </div>

          <article className="sn-duo-carte sn-duo-carte-clair" data-apparait>
            <h3>{t('landing.contraste.solution.titre')}</h3>
            <ul>
              {CONTRASTE.solution.map((cle) => (
                <li key={cle}>
                  <span className="sn-duo-marque sn-duo-marque-v-ok"><IconCheck size={12} /></span>
                  {avecLiens(cle)}
                </li>
              ))}
            </ul>
          </article>
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* 5. Fonctionnalités                                         */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-section sn-section-calme" id="fonctionnalites">
        <div className="sn-section-head" data-apparait>
          <span className="sn-eyebrow sn-eyebrow-dark">{t('landing.fonctionnalites.eyebrow')}</span>
          <h2>{t('landing.fonctionnalites.titre')}</h2>
          <p>{t('landing.fonctionnalites.texte')}</p>
        </div>
        <div className="sn-fonctionnalites">
          {FONCTIONNALITES.map(({ Icone, cleTitre, cleTexte, sujet }) => (
            <Link
              key={cleTitre}
              to={`/decouvrir/${sujet}`}
              className="sn-fonctionnalite sn-fonctionnalite-lien"
              data-apparait
            >
              <span className="sn-fonctionnalite-ic"><Icone size={19} /></span>
              <div>
                <h3>{t(cleTitre)}</h3>
                <p>{t(cleTexte)}</p>
                <span className="sn-lien-badge">{t('landing.enSavoirPlus')}</span>
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* 6. Aperçu de l'application (captures réelles)              */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-section" id="apercu">
        <div className="sn-section-head" data-apparait>
          <span className="sn-eyebrow sn-eyebrow-dark">{t('landing.apercu.eyebrow')}</span>
          <h2>{t('landing.apercu.titre')}</h2>
          <p>{t('landing.apercu.texte')}</p>
        </div>
        <div className="sn-apercu">
          {APERCU.map((capture) => (
            <article key={capture.img} className="sn-apercu-carte" data-apparait>
              <Link
                to={`/decouvrir/${capture.sujet}`}
                className="sn-apercu-image sn-apercu-lien"
                aria-label={`${t(capture.cleTitre)} — ${t('landing.enSavoirPlus')}`}
              >
                <img src={capture.img} alt={t(capture.cleAlt)} loading="lazy" />
                <span className="sn-lien-badge sn-lien-badge-img">{t('landing.enSavoirPlus')}</span>
              </Link>
              <div className="sn-apercu-corps">
                <h3>{t(capture.cleTitre)}</h3>
                <p>{avecLiens(capture.cleTexte)}</p>
              </div>
            </article>
          ))}
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* 7. Comment ça marche                                       */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-section sn-section-calme" id="etapes">
        <div className="sn-etapes-boite">
          <div className="sn-etapes-photo" data-apparait>
            <img
              src="/images/etapes.webp"
              alt={t('landing.etapes.photoAlt')}
              width="1200"
              height="1500"
              loading="lazy"
            />
          </div>
          <div>
            <div className="sn-etapes-tete" data-apparait>
              <span className="sn-eyebrow sn-eyebrow-dark">{t('landing.etapes.eyebrow')}</span>
              <h2>{t('landing.etapes.titre')}</h2>
              <p>{t('landing.etapes.texte')}</p>
            </div>
            <ol className="sn-etapes">
              {ETAPES.map((e) => (
                <li key={e.n} className="sn-etape" data-apparait>
                  <span className="sn-etape-num">{e.n}</span>
                  <div>
                    <h3>{t(e.cleTitre)}</h3>
                    <p>{avecLiens(e.cleTexte)}</p>
                  </div>
                </li>
              ))}
            </ol>
          </div>
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* 8. Appel à l'action                                        */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-band" id="demarrer">
        <img
          className="sn-band-fond"
          src="/images/cta.webp"
          alt=""
          aria-hidden="true"
          width="1600"
          height="900"
          loading="lazy"
        />
        <span className="sn-band-voile" aria-hidden="true" />
        <div className="sn-band-inner" data-apparait>
          <span className="sn-eyebrow sn-eyebrow-on-dark">
            <span className="sn-eyebrow-dot" />
            {t('landing.band.eyebrow')}
          </span>
          <h2>{t('landing.band.titre')}</h2>
          <p>{t('landing.band.texte')}</p>
          <Link to={cible} className="sn-btn sn-btn-green sn-btn-lg">
            {libelleCta}
            <IconArrowRight size={18} />
          </Link>
          <p className="sn-band-note">{t('landing.accesReserve')}</p>
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* 9. Contact                                                 */}
      {/* ---------------------------------------------------------- */}
      <section className="sn-section sn-section-calme" id="contact">
        <div className="sn-contact" data-apparait>
          <h2>{t('landing.contact.titre')}</h2>
          <p>{t('landing.contact.texte')}</p>
          <div className="sn-contact-liens">
            <a
              className="sn-btn sn-btn-green sn-btn-lg"
              href="mailto:malakjaouane2@gmail.com?subject=Portefeuille%20d%27initiatives"
            >
              {t('landing.contact.ecrire')}
            </a>
            <a className="sn-btn sn-btn-ghost sn-btn-lg" href="tel:+212682074679" dir="ltr">
              +212682074679
            </a>
          </div>
          <small>{t('landing.contact.projet')}</small>
        </div>
      </section>

      {/* ---------------------------------------------------------- */}
      {/* Pied de page                                                */}
      {/* ---------------------------------------------------------- */}
      <footer className="sn-footer">
        <div className="sn-footer-inner">
          <div className="sn-footer-brand">
            <div className="sn-logo">
              <img
                className="sn-logo-img"
                src="/images/logo-it6.png"
                alt="IT6 Academy"
                width="132"
                height="138"
                loading="lazy"
              />
              <span className="sn-logo-text">
                <strong>IT6 Consulting</strong>
                <small>{t('landing.brand.sousTitre')}</small>
              </span>
            </div>
            <p>{t('landing.footer.description')}</p>
          </div>
          <div className="sn-footer-cols">
            <div className="sn-footer-col">
              <h4>{t('landing.footer.colPlateforme')}</h4>
              <a href="#probleme">{t('landing.nav.probleme')}</a>
              <a href="#fonctionnalites">{t('landing.nav.fonctionnalites')}</a>
              <a href="#apercu">{t('landing.nav.apercu')}</a>
              <a href="#etapes">{t('landing.nav.etapes')}</a>
            </div>
            <div className="sn-footer-col">
              <h4>{t('landing.footer.colProfils')}</h4>
              <span>{t('landing.role.administrateur.nom')}</span>
              <span>{t('landing.role.responsable.nom')}</span>
              <span>{t('landing.role.visiteur.nom')}</span>
            </div>
            <div className="sn-footer-col">
              <h4>{t('landing.footer.colAcces')}</h4>
              <Link to={cible}>{libelleCta}</Link>
              <a href="#top">{t('landing.footer.hautPage')}</a>
            </div>
          </div>
        </div>
        <div className="sn-footer-bottom">
          {t('landing.footer.copyright', { annee: new Date().getFullYear() })}
        </div>
      </footer>

      {/* ---------------------------------------------------------- */}
      {/* Retour en haut (apparaît après un certain défilement)       */}
      {/* ---------------------------------------------------------- */}
      <button
        type="button"
        className={`sn-haut-page${hautPageVisible ? ' est-visible' : ''}`}
        onClick={allerEnHaut}
        aria-label={t('landing.footer.hautPage')}
        aria-hidden={!hautPageVisible}
        tabIndex={hautPageVisible ? 0 : -1}
      >
        <IconArrowUp size={20} />
      </button>
    </div>
  );
}
