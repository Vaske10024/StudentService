package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PredispitGradebookDTO {
    private Long predispitnaObavezaId;
    private String vrsta;
    private Integer maxPoeni;
    private List<PredispitStudentScoreDTO> studenti;
}
