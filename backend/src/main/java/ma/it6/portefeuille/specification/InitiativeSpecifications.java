package ma.it6.portefeuille.specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import ma.it6.portefeuille.dto.InitiativeFilterRequest;
import ma.it6.portefeuille.entity.Initiative;
import ma.it6.portefeuille.entity.StatutInitiative;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Spécifications JPA des filtres combinés de la liste des initiatives
 * (statut, entité, responsable, période, priorité, recherche textuelle).
 */
public final class InitiativeSpecifications {

    private InitiativeSpecifications() {
    }

    public static Specification<Initiative> filtrer(InitiativeFilterRequest filtre) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Par défaut, on n'affiche pas les initiatives archivées
            // (RM14 : l'archivage est un indicateur, pas un statut).
            if (filtre.archive() == null) {
                predicates.add(cb.equal(root.get("archive"), false));
            } else {
                predicates.add(cb.equal(root.get("archive"), filtre.archive()));
            }

            if (filtre.statut() != null) {
                Join<Initiative, StatutInitiative> statut = root.join("statut");
                predicates.add(cb.equal(statut.get("idStatut"), filtre.statut()));
            }

            if (filtre.entite() != null) {
                predicates.add(cb.equal(root.get("entite").get("idEntite"), filtre.entite()));
            }

            if (filtre.responsable() != null) {
                predicates.add(cb.equal(root.get("responsable").get("idUser"), filtre.responsable()));
            }

            if (filtre.priorite() != null) {
                predicates.add(cb.equal(root.get("priorite").get("idPriorite"), filtre.priorite()));
            }

            if (filtre.objectif() != null) {
                predicates.add(cb.equal(root.get("objectif").get("idObjectif"), filtre.objectif()));
            }

            // Période « du … au … » : on retient les initiatives ACTIVES sur
            // l'intervalle, c'est-à-dire celles dont la période de vie le
            // chevauche :
            //     dateDebut <= dateAu  ET  dateFinPrevue >= dateDu
            // Une initiative sans date est toujours conservée (on ne peut pas
            // décider qu'elle est hors période).
            if (filtre.dateDu() != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("dateFinPrevue")),
                        cb.greaterThanOrEqualTo(root.get("dateFinPrevue"), filtre.dateDu())));
            }
            if (filtre.dateAu() != null) {
                predicates.add(cb.or(
                        cb.isNull(root.get("dateDebut")),
                        cb.lessThanOrEqualTo(root.get("dateDebut"), filtre.dateAu())));
            }

            // Recherche textuelle sur le code ou le nom (insensible à la casse).
            if (filtre.recherche() != null && !filtre.recherche().isBlank()) {
                String motif = "%" + filtre.recherche().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), motif),
                        cb.like(cb.lower(root.get("nom")), motif)
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}