import { createContext, useContext, useEffect, useState, useCallback, useMemo } from 'react';
import dictionnaires, { LANGUES, LANGUE_DEFAUT } from './locales';

const I18nContext = createContext(null);
const CLE_STOCKAGE = 'pfa_langue';

/** Langue initiale : choix mémorisé, sinon langue par défaut. */
function langueInitiale() {
  try {
    const enr = localStorage.getItem(CLE_STOCKAGE);
    if (enr && LANGUES.some((l) => l.code === enr)) return enr;
  } catch {
    /* localStorage indisponible : on ignore */
  }
  return LANGUE_DEFAUT;
}

/**
 * Fournit la langue courante et la fonction de traduction `t(cle, params)`.
 * `t` accepte l'interpolation de variables : t('cle', { nom: 'IT6' })
 * remplace les `{nom}` présents dans le texte.
 */
export function I18nProvider({ children }) {
  const [langue, setLangueState] = useState(langueInitiale);

  const meta = useMemo(
    () => LANGUES.find((l) => l.code === langue) ?? LANGUES[0],
    [langue]
  );

  useEffect(() => {
    try {
      localStorage.setItem(CLE_STOCKAGE, langue);
    } catch {
      /* ignore */
    }
    const html = document.documentElement;
    html.lang = langue;
    html.dir = meta.rtl ? 'rtl' : 'ltr';
  }, [langue, meta]);

  const setLangue = useCallback((code) => {
    if (LANGUES.some((l) => l.code === code)) setLangueState(code);
  }, []);

  const t = useCallback(
    (cle, params) => {
      const dict = dictionnaires[langue] ?? {};
      let texte = dict[cle];
      if (texte === undefined) texte = dictionnaires[LANGUE_DEFAUT]?.[cle];
      if (texte === undefined) return cle;
      if (params) {
        texte = String(texte).replace(/\{(\w+)\}/g, (m, k) =>
          params[k] !== undefined && params[k] !== null ? String(params[k]) : m
        );
      }
      return texte;
    },
    [langue]
  );

  const valeur = useMemo(
    () => ({ langue, setLangue, t, langues: LANGUES, dir: meta.rtl ? 'rtl' : 'ltr' }),
    [langue, setLangue, t, meta]
  );

  return <I18nContext.Provider value={valeur}>{children}</I18nContext.Provider>;
}

export function useI18n() {
  const ctx = useContext(I18nContext);
  if (!ctx) throw new Error('useI18n doit être utilisé dans <I18nProvider>');
  return ctx;
}
