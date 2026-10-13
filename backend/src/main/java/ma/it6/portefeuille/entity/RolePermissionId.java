package ma.it6.portefeuille.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

/**
 * Clé composite de l'association Rôle <-> Permission.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RolePermissionId implements Serializable {

    @Column(name = "id_role")
    private Long idRole;

    @Column(name = "id_permission")
    private Long idPermission;
}