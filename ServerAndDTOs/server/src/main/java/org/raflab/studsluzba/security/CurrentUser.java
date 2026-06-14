package org.raflab.studsluzba.security;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.PredispitnaObaveza;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.repositories.schedule.StudentGroupMembershipRepository;
import org.raflab.studsluzba.repositories.schedule.ClassSessionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CurrentUser {
    private final UserAccountRepository userRepo;
    private final StudentIndeksRepository indeksRepo;
    private final DrziPredmetRepository drziRepo;
    private final IspitRepository ispitRepo;
    private final IspitQueryRepository prijavaRepo;
    private final PredispitnaObavezaRepository predObRepo;
    private final StudentGroupMembershipRepository groupMembershipRepo;
    private final ClassSessionRepository classSessionRepo;

    public UserAccount account() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Korisnik nije prijavljen.");
        }
        return userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Korisnički nalog nije pronađen."));
    }

    public Long userId() {
        return account().getId();
    }

    public Role role() {
        return account().getRole();
    }

    public boolean isAdmin() {
        return role() == Role.ADMIN;
    }

    public boolean isStudent() {
        return role() == Role.STUDENT;
    }

    public boolean isProfessor() {
        return role() == Role.PROFESSOR;
    }

    public Long linkedStudentIndeksId() {
        UserAccount ua = account();
        return ua.getLinkedStudentIndeks() == null ? null : ua.getLinkedStudentIndeks().getId();
    }

    public Long linkedNastavnikId() {
        UserAccount ua = account();
        return ua.getLinkedNastavnik() == null ? null : ua.getLinkedNastavnik().getId();
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("Potrebna je ADMIN uloga.");
        }
    }

    public void requireAdminOrStudentOwnsIndeks(Long indeksId) {
        if (isAdmin()) return;
        requireStudentOwnsIndeks(indeksId);
    }

    public void requireStudentOwnsIndeks(Long indeksId) {
        UserAccount ua = account();
        if (ua.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Potrebna je STUDENT uloga.");
        }
        if (indeksId == null) {
            throw new AccessDeniedException("Nedostaje indeks.");
        }
        if (ua.getLinkedStudentIndeks() != null && indeksId.equals(ua.getLinkedStudentIndeks().getId())) {
            return;
        }
        if (ua.getLinkedStudentPodaci() != null) {
            List<StudentIndeks> all = indeksRepo.findStudentIndeksiForStudentPodaciId(ua.getLinkedStudentPodaci().getId());
            boolean owns = all.stream().anyMatch(si -> indeksId.equals(si.getId()));
            if (owns) return;
        }
        throw new AccessDeniedException("Student može pristupiti samo sopstvenom indeksu.");
    }

    public void requireProfessorOwnsDrziPredmet(Long drziPredmetId) {
        if (isAdmin()) return;
        UserAccount ua = account();
        if (ua.getRole() != Role.PROFESSOR || ua.getLinkedNastavnik() == null) {
            throw new AccessDeniedException("Potrebna je PROFESSOR uloga.");
        }
        DrziPredmet dp = drziRepo.findById(drziPredmetId)
                .orElseThrow(() -> new NoSuchElementException("DrziPredmet ne postoji: " + drziPredmetId));
        Long linkedNastavnikId = ua.getLinkedNastavnik().getId();
        Long ownerId = dp.getNastavnik() == null ? null : dp.getNastavnik().getId();
        if (!linkedNastavnikId.equals(ownerId)) {
            throw new AccessDeniedException("Profesor može pristupiti samo predmetima koje predaje.");
        }
    }

    public void requireAdminOrProfessorOwnsIspit(Long ispitId) {
        if (isAdmin()) return;
        requireProfessorOwnsIspit(ispitId);
    }

    public void requireProfessorOwnsIspit(Long ispitId) {
        Ispit ispit = ispitRepo.findById(ispitId)
                .orElseThrow(() -> new NoSuchElementException("Ispit ne postoji: " + ispitId));
        if (ispit.getDrziPredmet() == null || ispit.getDrziPredmet().getId() == null) {
            throw new AccessDeniedException("Ispit nema nastavnu dodelu.");
        }
        requireProfessorOwnsDrziPredmet(ispit.getDrziPredmet().getId());
    }

    public void requireAdminOrLeadProfessorOwnsIspit(Long ispitId) {
        if (isAdmin()) return;
        Ispit ispit = ispitRepo.findById(ispitId)
                .orElseThrow(() -> new NoSuchElementException("Ispit ne postoji: " + ispitId));
        if (ispit.getDrziPredmet() == null || ispit.getDrziPredmet().getUloga() != DrziPredmet.Uloga.NOSILAC) {
            throw new AccessDeniedException("Samo nosilac predmeta ili admin može zaključati rezultate.");
        }
        requireProfessorOwnsDrziPredmet(ispit.getDrziPredmet().getId());
    }

    public void requireAdminOrProfessorOwnsPrijava(Long prijavaId) {
        if (isAdmin()) return;
        PrijavaIspita pi = prijavaRepo.findById(prijavaId)
                .orElseThrow(() -> new NoSuchElementException("Prijava ne postoji: " + prijavaId));
        if (pi.getIspit() == null || pi.getIspit().getId() == null) {
            throw new AccessDeniedException("Prijava nije vezana za ispit koji profesor može menjati.");
        }
        requireProfessorOwnsIspit(pi.getIspit().getId());
    }

    public void requireAdminOrProfessorOwnsPredispitnaObaveza(Long predObId) {
        if (isAdmin()) return;
        PredispitnaObaveza po = predObRepo.findById(predObId)
                .orElseThrow(() -> new NoSuchElementException("Predispitna obaveza ne postoji: " + predObId));
        Long predmetId = po.getPredmet() == null ? null : po.getPredmet().getId();
        Long sgId = po.getSkolskaGodina() == null ? null : po.getSkolskaGodina().getId();
        Long nastavnikId = linkedNastavnikId();
        if (predmetId == null || sgId == null || nastavnikId == null) {
            throw new AccessDeniedException("Profesor nije povezan sa ovom predispitnom obavezom.");
        }
        if (!drziRepo.existsProfessorAssignment(predmetId, nastavnikId, sgId)) {
            throw new AccessDeniedException("Profesor može menjati samo predispitne obaveze za svoje predmete.");
        }
    }

    public void requireAdminOrProfessorOwnsPredmetInYear(Long predmetId, Long skolskaGodinaId) {
        if (isAdmin()) return;
        Long nastavnikId = linkedNastavnikId();
        if (!isProfessor() || nastavnikId == null || !drziRepo.existsProfessorAssignment(predmetId, nastavnikId, skolskaGodinaId)) {
            throw new AccessDeniedException("Profesor može menjati predispitne obaveze samo za svoje predmete.");
        }
    }

    public void requireCanAccessScheduleGroup(Long groupId) {
        if (isAdmin()) return;
        if (isStudent() && linkedStudentIndeksId() != null
                && groupMembershipRepo.existsByStudentGroupIdAndStudentIndeksId(groupId, linkedStudentIndeksId())) return;
        if (isProfessor() && linkedNastavnikId() != null
                && classSessionRepo.existsByStudentGroupIdAndProfessorId(groupId, linkedNastavnikId())) return;
        throw new AccessDeniedException("Korisnik nema pristup rasporedu ove grupe.");
    }
}
