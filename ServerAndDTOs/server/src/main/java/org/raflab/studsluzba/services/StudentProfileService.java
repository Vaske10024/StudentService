package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.SlusaPredmetRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.StudentPodaciRepository;
import org.raflab.studsluzba.utils.EntityMappers;
import org.raflab.studsluzba.utils.PrijavaMappers;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentProfileService {

    private final StudentIndeksRepository studentIndeksRepo;
    private final StudentPodaciRepository studentPodaciRepo;
    private final SlusaPredmetRepository slusaPredmetRepo;
    private final EntityMappers entityMappers;
    private final StudentIspitiViewService ispitiViewService;
    private final IspitQueryRepository prijavaRepo;
    private final PrijavaMappers prijavaMappers;
    private final FinanceService financeService;
    private final UpisObnovaService upisObnovaService;
    private final SkolskaGodinaService skolskaGodinaService;
    private final StudentLifecycleService studentLifecycleService;

    public StudentProfileDTO getStudentProfile(Long indeksId) {
        StudentIndeks studIndeks = requireIndeks(indeksId);
        StudentProfileDTO dto = new StudentProfileDTO();
        dto.setIndeks(entityMappers.fromStudentIndexToResponse(studIndeks));
        dto.setSlusaPredmete(currentSubjects(indeksId));
        Page<PredmetDTO> nepolozeni = ispitiViewService.nepolozeniPaged(
                studIndeks.getStudProgramOznaka(), studIndeks.getGodina(), studIndeks.getBroj(), 0, 1000);
        dto.setNepolozeniPredmeti(nepolozeni.getContent());
        return dto;
    }

    public StudentWebProfileDTO getStudentWebProfile(Long indeksId) {
        StudentIndeks studIndeks = requireIndeks(indeksId);
        Long studPodaciId = studIndeks.getStudent().getId();
        StudentIndeks aktivan = activeIndexForStudent(studPodaciId);

        StudentWebProfileDTO dto = new StudentWebProfileDTO();
        dto.setAktivanIndeks(entityMappers.fromStudentIndexToResponse(aktivan == null ? studIndeks : aktivan));
        dto.setSlusaPredmete(currentSubjects(indeksId));
        return dto;
    }

    public StudentDashboardDTO getStudentDashboard(Long indeksId) {
        StudentIndeks indeks = requireIndeks(indeksId);
        Long studentPodaciId = indeks.getStudent().getId();
        StudentIndeks active = activeIndexForStudent(studentPodaciId);
        StudentIndeks dashboardIndex = active == null ? indeks : active;

        StudentDashboardDTO dto = new StudentDashboardDTO();
        dto.setStudent(entityMappers.fromStudentPodaciToResponse(indeks.getStudent()));
        dto.setActiveIndex(entityMappers.fromStudentIndexToResponse(dashboardIndex));
        dto.setAllIndexes(studentIndeksRepo.findStudentIndeksiForStudentPodaciId(studentPodaciId).stream()
                .map(entityMappers::fromStudentIndexToResponse)
                .collect(Collectors.toList()));
        dto.setCurrentSubjects(currentSubjectDetails(dashboardIndex.getId()));

        dto.setPassedSubjects(ispitiViewService.polozenePaged(
                dashboardIndex.getStudProgramOznaka(), dashboardIndex.getGodina(), dashboardIndex.getBroj(), 0, 1000
        ).getContent());
        dto.setFailedOrNotPassedSubjects(ispitiViewService.nepolozeniPaged(
                dashboardIndex.getStudProgramOznaka(), dashboardIndex.getGodina(), dashboardIndex.getBroj(), 0, 1000
        ).getContent());
        dto.setActiveExamRegistrations(prijavaRepo.activeRegistrationsForStudent(dashboardIndex.getId()).stream()
                .map(prijavaMappers::toResponse)
                .collect(Collectors.toList()));
        dto.setPreviousExamAttempts(prijavaRepo.previousAttemptsForStudent(dashboardIndex.getId()).stream()
                .map(prijavaMappers::toResponse)
                .collect(Collectors.toList()));
        dto.setStudyEnrollments(upisObnovaService.readUpisi(dashboardIndex.getId()).stream()
                .map(u -> new UpisanaGodinaDTO(u.getId(), "UPIS", u.getUpisujeGodinu(), u.getDatum(), u.getNapomena()))
                .collect(Collectors.toList()));
        dto.setRenewals(upisObnovaService.readObnoveDto(dashboardIndex.getId()));
        dto.setPayments(financeService.ledgerDto(dashboardIndex.getId()));
        dto.setBalance(financeService.balance(dashboardIndex.getId()));
        dto.setSchoolYear(entityMappers.fromSkolskaGodinaToDTO(skolskaGodinaService.active()));
        dto.setStatus(studentLifecycleService.getStatus(dashboardIndex.getId()));
        dto.setStatusHistory(studentLifecycleService.history(dashboardIndex.getId()));
        return dto;
    }

    public List<SlusaPredmetDTO> currentSubjects(Long indeksId) {
        return slusaPredmetRepo.getSlusaPredmetForIndeksAktivnaGodina(indeksId)
                .stream()
                .map(entityMappers::fromSlusaPredmetToDTO)
                .collect(Collectors.toList());
    }

    public List<StudentSubjectDTO> currentSubjectDetails(Long indeksId) {
        return slusaPredmetRepo.getSlusaPredmetForIndeksAktivnaGodina(indeksId)
                .stream()
                .map(entityMappers::fromSlusaPredmetToStudentSubjectDTO)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(StudentSubjectDTO::getSemester,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(StudentSubjectDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .collect(Collectors.toList());
    }

    private StudentIndeks requireIndeks(Long indeksId) {
        return studentIndeksRepo.findById(indeksId)
                .orElseThrow(() -> new RuntimeException("StudentIndeks not found: id=" + indeksId));
    }

    private StudentIndeks activeIndexForStudent(Long studentPodaciId) {
        List<StudentIndeks> aktivniIndeksi = studentPodaciRepo.getAktivanIndeks(studentPodaciId);
        if (aktivniIndeksi == null || aktivniIndeksi.isEmpty()) return null;
        return aktivniIndeksi.stream()
                .max(Comparator.comparingInt(si -> si.getGodina() == null ? 0 : si.getGodina()))
                .orElse(null);
    }
}
