-- ============================================================
-- Nettoyage des données laissées par la suite de tests API
-- (database/tests-api.ps1).
-- ------------------------------------------------------------
-- Supprime uniquement les artefacts de test :
--   * initiatives de test, archivées ou non (code préfixé TEST) ;
--   * initiatives issues de la transformation de demandes de test
--     (code DEM-xxxxxx, demandes au titre préfixé TEST-DEM) ;
--   * demandes d'initiatives de test ;
--   * objectifs stratégiques de test (code préfixé TEST-OBJ) ;
--   * comptes de test désactivés (email test*@it6.ma).
-- Les données de démonstration et les données utilisateur réelles
-- ne sont jamais touchées.
-- Usage :
--   mysql -u pfa_user -p portefeuille_initiatives < database/cleanup-test-data.sql
-- ============================================================

-- Les étapes et jalons liés sont supprimés en cascade (ON DELETE CASCADE).
--
-- On ne requiert PAS archive = 1 : une exécution interrompue (ex. refus
-- d'archivage en fin de parcours) laisse une initiative de test non archivée,
-- et le nettoyage doit pouvoir la retirer aussi. Le préfixe TEST est réservé
-- à la suite de tests — les codes de démonstration (DIG-CLI-001…) commencent
-- tous autrement.
DELETE FROM initiative
WHERE code LIKE 'TEST%';

-- Initiatives issues de la transformation de demandes de test (avant la
-- suppression des demandes elles-mêmes pour conserver la liaison).
DELETE ini FROM initiative ini
JOIN demande_initiative d ON d.id_initiative_liee = ini.id_initiative
WHERE d.titre LIKE 'TEST-DEM%';

DELETE FROM demande_initiative
WHERE titre LIKE 'TEST-DEM%';

DELETE FROM objectif_strategique
WHERE code LIKE 'TEST-OBJ%';

DELETE FROM utilisateur
WHERE email LIKE 'test%@it6.ma';
