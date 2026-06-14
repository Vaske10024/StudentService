package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PredispitStudentScoreDTO {
    private Long studentIndeksId;
    private Long studentId;
    private String ime;
    private String prezime;
    private String studProgramOznaka;
    private Integer broj;
    private Integer godina;
    private Integer poeni;
}
