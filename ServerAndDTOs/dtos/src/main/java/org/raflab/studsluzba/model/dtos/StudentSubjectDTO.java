package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudentSubjectDTO {
    private Long listeningId;
    private Long realizationId;
    private Long subjectId;
    private String code;
    private String name;
    private String description;
    private Integer ects;
    private Integer studyYear;
    private Integer semester;
    private String programCode;
    private String schoolYear;
    private String realizationStatus;
    private List<DrziPredmetDTO> instructors = new ArrayList<>();
}
