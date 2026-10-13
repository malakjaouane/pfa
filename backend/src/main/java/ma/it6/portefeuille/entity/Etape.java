package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Étape de planification d'une initiative (une étape appartient à
 * une seule initiative).
 */
@Entity
@Table(name = "etape")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etape extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etape")
    private Long idEtape;

    @Column(name = "nom", nullable = false, length = 200)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;

    @Column(name = "avancement", nullable = false)
    private Integer avancement = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_initiative")
    private Initiative initiative;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_etape")
    private StatutEtape statutEtape;
}