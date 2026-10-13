package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Jalon : échéance ou événement important d'une initiative (un jalon
 * appartient à une seule initiative). La date de réalisation peut être
 * vide tant que le jalon n'est pas atteint (RM15).
 */
@Entity
@Table(name = "jalon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jalon extends BaseAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jalon")
    private Long idJalon;

    @Column(name = "nom", nullable = false, length = 200)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_prevue")
    private LocalDate datePrevue;

    @Column(name = "date_realisation")
    private LocalDate dateRealisation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_initiative")
    private Initiative initiative;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_statut_jalon")
    private StatutJalon statutJalon;
}