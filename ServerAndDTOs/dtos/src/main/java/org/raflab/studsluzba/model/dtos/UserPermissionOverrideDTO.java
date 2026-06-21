package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPermissionOverrideDTO {
    private Long id;
    private Long userId;
    private String username;
    private String permission;
    private boolean allowed;
}
