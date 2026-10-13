package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Référentiel des statuts d'une initiative.
 * Statuts métier : À planifier, En cours, En pause, Terminée, Annulée.
 * L'archivage n'est PAS un statut (géré par le champ « archive »).
 */
@Entity
@Table(name = "statut_initiative")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutInitiative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut")
    private Long idStatut;

    @Column(name = "libelle", nullable = false, unique = true, length = 50)
    private String libelle;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;
}