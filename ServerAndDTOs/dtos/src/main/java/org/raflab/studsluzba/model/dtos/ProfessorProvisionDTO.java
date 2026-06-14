package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfessorProvisionDTO {
    private Long professorId;
    private Long accountId;
    private String username;
    private String temporaryPassword;
    private boolean accountCreated;
}
