package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentIndeksLiteDTO {
    private Long id;
    private Integer broj;
    private Integer godina;
    private String studProgramOznaka;

    private Long studentId;
    private String ime;
    private String prezime;
}
