package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadDTO {
    private Long id;
    private String initials;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String interestedProgram;
    private String source;
    private String note;
    private LocalDateTime createdAt;
    private boolean fullAccess;
}
