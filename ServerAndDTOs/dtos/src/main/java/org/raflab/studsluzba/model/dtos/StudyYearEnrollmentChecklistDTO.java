package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class StudyYearEnrollmentChecklistDTO {
    private boolean contractReceived;
    private boolean paymentConfirmed;
    private boolean documentationComplete;

    @Size(max = 2000)
    private String note;
}
