package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class StudyYearEnrollmentDecisionDTO {
    @NotBlank
    @Size(max = 2000)
    private String reason;
}
