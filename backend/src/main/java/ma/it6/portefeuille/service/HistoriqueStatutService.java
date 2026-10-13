package ma.it6.portefeuille.service;

import ma.it6.portefeuille.dto.HistoriqueStatutResponse;
import ma.it6.portefeuille.entity.HistoriqueStatut;
import ma.it6.portefeuille.entity.Initiative;
import ma.it6.portefeuille.entity.Utilisateur;
import ma.it6.portefeuille.mapper.UtilisateurMapper;
import ma.it6.portefeuille.repository.HistoriqueStatutRepository;
import ma.it6.portefeuille.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Journal des transitions de statut des initiatives.
 *
 * <p>Chaque changement de cycle de vie (création, modification, glisser-déposer
 * sur le Kanban, archivage) dépose une ligne ici. Le journal est écrit dans la
 * <strong>même transaction</strong> que la modification de l'initiative : il ne
 * peut donc jamais exister une transition sans trace.</p>
 */
@Service
public class HistoriqueStatutService {

    /** Longueur maximale du motif en base ; au-delà, on tronque proprement. */
    private static final int LONGUEUR_MAX_COMMENTAIRE = 500;

    private final HistoriqueStatutRepository historiqueRepository;
    private final UtilisateurRepository utilisateurRepository;

    public HistoriqueStatutService(HistoriqueStatutRepository historiqueRepository,
                                   UtilisateurRepository utilisateurRepository) {
        this.historiqueRepository = historiqueRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Dépose une ligne dans le journal.
     *
     * @param ancienStatut   statut avant la transition, {@code null} à la création
     * @param nouveauStatut  statut après la transition
     * @param emailUtilisateur e-mail de l'auteur ; peut être {@code null} pour
     *                       les transitions automatiques (transformation d'une
     *                       demande acceptée, alignement de démarrage)
     */
    @Transactional
    public void enregistrer(Initiative initiative,
                            String ancienStatut,
                            String nouveauStatut,
                            String commentaire,
                            String emailUtilisateur) {
        HistoriqueStatut entree = new HistoriqueStatut();
        entree.setInitiative(initiative);
        entree.setAncienStatut(ancienStatut);
        entree.setNouveauStatut(nouveauStatut);
        entree.setCommentaire(tronquer(commentaire));
        entree.setDateChangement(LocalDateTime.now());
        entree.setUtilisateur(resoudreUtilisateur(emailUtilisateur));
        historiqueRepository.save(entree);
    }

    /** Journal complet d'une initiative, du plus récent au plus ancien. */
    @Transactional(readOnly = true)
    public List<HistoriqueStatutResponse> historique(Long idInitiative) {
        return historiqueRepository
                .findByInitiativeIdInitiativeOrderByDateChangementDesc(idInitiative)
                .stream()
                .map(HistoriqueStatutService::toResponse)
                .toList();
    }

    // ------------------------------------------------------------------
    // Internes
    // ------------------------------------------------------------------

    /**
     * L'auteur peut avoir été supprimé entre-temps (la clé étrangère est en
     * ON DELETE SET NULL) : on laisse alors le champ vide plutôt que de faire
     * échouer la lecture du journal.
     */
    private Utilisateur resoudreUtilisateur(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return utilisateurRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
    }

    private static String tronquer(String commentaire) {
        if (commentaire == null || commentaire.isBlank()) {
            return null;
        }
        String nettoye = commentaire.trim();
        return nettoye.length() > LONGUEUR_MAX_COMMENTAIRE
                ? nettoye.substring(0, LONGUEUR_MAX_COMMENTAIRE)
                : nettoye;
    }

    private static HistoriqueStatutResponse toResponse(HistoriqueStatut h) {
        return new HistoriqueStatutResponse(
                h.getIdHistorique(),
                h.getAncienStatut(),
                h.getNouveauStatut(),
                h.getCommentaire(),
                h.getDateChangement(),
                h.getUtilisateur() == null ? null : UtilisateurMapper.toSummary(h.getUtilisateur()));
    }
}
