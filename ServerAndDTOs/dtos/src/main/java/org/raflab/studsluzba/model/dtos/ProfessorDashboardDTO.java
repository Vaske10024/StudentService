package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProfessorDashboardDTO {
    private AuthUserDTO user;
    private List<DrziPredmetLiteDTO> subjects = new ArrayList<>();
    private List<IspitDTO> exams = new ArrayList<>();
}
