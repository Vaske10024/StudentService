package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudentDashboardDTO {
    private StudentPodaciResponse student;
    private StudentIndeksResponse activeIndex;
    private List<StudentIndeksResponse> allIndexes = new ArrayList<>();
    private List<StudentSubjectDTO> currentSubjects = new ArrayList<>();
    private List<PolozenPredmetDTO> passedSubjects = new ArrayList<>();
    private List<PredmetDTO> failedOrNotPassedSubjects = new ArrayList<>();
    private List<PrijavaResponseDTO> activeExamRegistrations = new ArrayList<>();
    private List<PrijavaResponseDTO> previousExamAttempts = new ArrayList<>();
    private List<UpisanaGodinaDTO> studyEnrollments = new ArrayList<>();
    private List<UpisanaGodinaDTO> renewals = new ArrayList<>();
    private List<UplataDTO> payments = new ArrayList<>();
    private SaldoResponse balance;
    private SkolskaGodinaDTO schoolYear;
    private StudentStatusDTO status;
    private List<StudentStatusHistoryDTO> statusHistory = new ArrayList<>();
}
