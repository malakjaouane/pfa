package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Référentiel des statuts d'une demande d'initiative (workflow SPM) :
 * Brouillon -> Soumise -> En évaluation -> Acceptée / Refusée.
 */
@Entity
@Table(name = "statut_demande")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutDemande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_demande")
    private Long idStatutDemande;

    @Column(name = "libelle", nullable = false, unique = true, length = 50)
    private String libelle;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;
}