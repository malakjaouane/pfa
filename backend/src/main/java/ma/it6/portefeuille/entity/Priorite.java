package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Référentiel des niveaux de priorité d'une initiative.
 */
@Entity
@Table(name = "priorite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Priorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_priorite")
    private Long idPriorite;

    @Column(name = "libelle", nullable = false, unique = true, length = 50)
    private String libelle;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;
}