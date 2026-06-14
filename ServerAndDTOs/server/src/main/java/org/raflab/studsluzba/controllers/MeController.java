package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.StudentProfileService;
import org.raflab.studsluzba.services.PermissionService;
import org.raflab.studsluzba.services.AvailableExamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {
    private final CurrentUser currentUser;
    private final StudentProfileService studentProfileService;
    private final DrziPredmetRepository drziPredmetRepository;
    private final IspitRepository ispitRepository;
    private final PermissionService permissionService;
    private final AvailableExamService availableExamService;

    @GetMapping
    public AuthUserDTO me() {
        return AuthController.toDto(currentUser.account(), permissionService.currentPermissions());
    }

    @GetMapping("/student/dashboard")
    public StudentDashboardDTO studentDashboard() {
        return studentProfileService.getStudentDashboard(requireLinkedStudentIndeksId());
    }

    @GetMapping("/student/profile")
    public StudentProfileDTO studentProfile() {
        return studentProfileService.getStudentProfile(requireLinkedStudentIndeksId());
    }

    @GetMapping("/student/subjects")
    public List<StudentSubjectDTO> studentSubjects() {
        return studentProfileService.currentSubjectDetails(requireLinkedStudentIndeksId());
    }

    @GetMapping("/student/exams")
    public StudentDashboardDTO studentExams() {
        StudentDashboardDTO dto = studentProfileService.getStudentDashboard(requireLinkedStudentIndeksId());
        StudentDashboardDTO result = new StudentDashboardDTO();
        result.setActiveIndex(dto.getActiveIndex());
        result.setActiveExamRegistrations(dto.getActiveExamRegistrations());
        result.setPreviousExamAttempts(dto.getPreviousExamAttempts());
        return result;
    }

    @GetMapping("/student/available-exams")
    public List<AvailableExamDTO> availableStudentExams() {
        return availableExamService.forStudent(requireLinkedStudentIndeksId());
    }

    @GetMapping("/student/payments")
    public StudentDashboardDTO studentPayments() {
        StudentDashboardDTO dto = studentProfileService.getStudentDashboard(requireLinkedStudentIndeksId());
        StudentDashboardDTO result = new StudentDashboardDTO();
        result.setActiveIndex(dto.getActiveIndex());
        result.setPayments(dto.getPayments());
        result.setBalance(dto.getBalance());
        return result;
    }

    @GetMapping("/professor/dashboard")
    public ProfessorDashboardDTO professorDashboard() {
        ProfessorDashboardDTO dto = new ProfessorDashboardDTO();
        dto.setUser(AuthController.toDto(currentUser.account(), permissionService.currentPermissions()));
        dto.setSubjects(professorSubjects());
        dto.setExams(professorExams());
        return dto;
    }

    @GetMapping("/professor/subjects")
    public List<DrziPredmetLiteDTO> professorSubjects() {
        Long nastavnikId = requireLinkedNastavnikId();
        return drziPredmetRepository.findActiveByNastavnikId(nastavnikId).stream()
                .map(this::toDrziLite)
                .collect(Collectors.toList());
    }

    @GetMapping("/professor/exams")
    public List<IspitDTO> professorExams() {
        Long nastavnikId = requireLinkedNastavnikId();
        return ispitRepository.findByNastavnikId(nastavnikId).stream()
                .map(this::toIspitDTO)
                .collect(Collectors.toList());
    }

    private Long requireLinkedStudentIndeksId() {
        UserAccount ua = currentUser.account();
        if (ua.getLinkedStudentIndeks() == null) {
            throw ApiException.forbidden("Student nalog nije povezan sa indeksom.");
        }
        return ua.getLinkedStudentIndeks().getId();
    }

    private Long requireLinkedNastavnikId() {
        UserAccount ua = currentUser.account();
        if (ua.getLinkedNastavnik() == null) {
            throw ApiException.forbidden("Profesor nalog nije povezan sa nastavnikom.");
        }
        return ua.getLinkedNastavnik().getId();
    }

    private DrziPredmetLiteDTO toDrziLite(DrziPredmet dp) {
        DrziPredmetLiteDTO dto = new DrziPredmetLiteDTO();
        dto.setId(dp.getId());
        if (dp.getPredmet() != null) {
            dto.setPredmetId(dp.getPredmet().getId());
            dto.setPredmetNaziv(dp.getPredmet().getNaziv());
        }
        if (dp.getNastavnik() != null) {
            dto.setNastavnikId(dp.getNastavnik().getId());
            dto.setNastavnikImePrezime(dp.getNastavnik().getIme() + " " + dp.getNastavnik().getPrezime());
        }
        if (dp.getSkolskaGodina() != null) {
            dto.setSkolskaGodinaId(dp.getSkolskaGodina().getId());
            dto.setSkolskaGodinaNaziv(dp.getSkolskaGodina().getGodina());
        }
        return dto;
    }

    private IspitDTO toIspitDTO(Ispit i) {
        IspitDTO dto = new IspitDTO();
        dto.setId(i.getId());
        dto.setDatumOdrzavanja(i.getDatumOdrzavanja());
        dto.setVremePocetka(i.getVremePocetka());
        dto.setZakljucen(i.isZakljucen());
        if (i.getIspitniRok() != null) {
            dto.setIspitniRokId(i.getIspitniRok().getId());
            dto.setRokDatumPocetka(i.getIspitniRok().getDatumPocetka());
            dto.setRokDatumZavrsetka(i.getIspitniRok().getDatumZavrsetka());
        }
        if (i.getDrziPredmet() != null) {
            dto.setDrziPredmetId(i.getDrziPredmet().getId());
            if (i.getDrziPredmet().getPredmet() != null) dto.setPredmetNaziv(i.getDrziPredmet().getPredmet().getNaziv());
            if (i.getDrziPredmet().getNastavnik() != null) dto.setNastavnikImePrezime(i.getDrziPredmet().getNastavnik().getIme() + " " + i.getDrziPredmet().getNastavnik().getPrezime());
        }
        return dto;
    }
}
