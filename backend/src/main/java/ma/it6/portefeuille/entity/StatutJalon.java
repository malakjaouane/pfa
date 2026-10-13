package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Référentiel des statuts d'un jalon.
 */
@Entity
@Table(name = "statut_jalon")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutJalon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_jalon")
    private Long idStatutJalon;

    @Column(name = "libelle", nullable = false, unique = true, length = 50)
    private String libelle;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;
}