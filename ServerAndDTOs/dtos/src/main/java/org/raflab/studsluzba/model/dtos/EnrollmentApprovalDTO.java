package org.raflab.studsluzba.model.dtos;
import lombok.Data;
import javax.validation.constraints.NotBlank;
@Data
public class EnrollmentApprovalDTO {
    @NotBlank private String initialPassword;
}
