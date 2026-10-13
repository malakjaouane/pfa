package ma.it6.portefeuille.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Changement de statut déclenché depuis le tableau Kanban (glisser-déposer)
 * ou depuis la fiche détaillée. Le motif est facultatif mais fortement
 * conseillé : il alimente le journal des transitions.
 */
public record ChangerStatutRequest(
        @NotNull(message = "Le statut cible est obligatoire") Long idStatut,
        String commentaire
) {
}
