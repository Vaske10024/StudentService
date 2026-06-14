package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class StudyYearEnrollmentRequestUpdateDTO {
    @NotNull
    private Set<Long> transferredSubjectIds = new LinkedHashSet<>();

    @Size(max = 2000)
    private String studentNote;
}
