package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 180)
    private String email;

    @Size(max = 60)
    private String phone;

    @Size(max = 180)
    private String interestedProgram;

    @Size(max = 180)
    private String source;

    @Size(max = 1000)
    private String note;

    @AssertTrue(message = "Privacy consent is required.")
    private boolean privacyConsent;
}
