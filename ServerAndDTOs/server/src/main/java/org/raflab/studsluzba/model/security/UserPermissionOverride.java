package org.raflab.studsluzba.model.security;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
@Entity @Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(name="uk_user_permission_override",columnNames={"user_account_id","permission"}))
public class UserPermissionOverride {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) private UserAccount userAccount;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=64) private Permission permission;
 @Column(nullable=false) private boolean allowed;
}
