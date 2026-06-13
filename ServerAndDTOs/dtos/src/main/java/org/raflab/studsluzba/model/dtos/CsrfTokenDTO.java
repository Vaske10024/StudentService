package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CsrfTokenDTO {
    private String headerName;
    private String parameterName;
    private String token;
}
