package org.raflab.studsluzba.model.security;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
@Entity @Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(name="uk_role_permission",columnNames={"role","permission"}))
public class RolePermission {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private Role role;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=64) private Permission permission;
}
