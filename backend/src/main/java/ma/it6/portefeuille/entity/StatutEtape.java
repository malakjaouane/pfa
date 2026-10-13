package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Référentiel des statuts d'une étape.
 */
@Entity
@Table(name = "statut_etape")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatutEtape {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_statut_etape")
    private Long idStatutEtape;

    @Column(name = "libelle", nullable = false, unique = true, length = 50)
    private String libelle;

    @Column(name = "ordre", nullable = false)
    private Integer ordre;
}