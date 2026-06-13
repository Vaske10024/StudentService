package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDTO {
    private Long id;
    private String username;
    private String role;
    private boolean enabled;
    private Long linkedStudentPodaciId;
    private Long linkedStudentIndeksId;
    private Long linkedNastavnikId;
}
