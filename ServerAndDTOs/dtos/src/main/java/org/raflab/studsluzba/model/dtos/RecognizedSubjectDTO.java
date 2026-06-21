package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecognizedSubjectDTO {
    private Long id;
    private Long indeksId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Integer grade;
    private Integer ects;
    private String source;
    private Long approvedByUserId;
    private LocalDateTime createdAt;
}
