package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentIndexProvisionDTO {
    private Long indexId;
    private String username;
    private String temporaryPassword;
    private boolean accountCreated;
}
