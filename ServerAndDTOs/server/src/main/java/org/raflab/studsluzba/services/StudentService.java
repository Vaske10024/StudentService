package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.StudentPodaciRepository;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.utils.Converters;
import org.raflab.studsluzba.utils.EntityMappers;
import org.raflab.studsluzba.utils.ParseUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentPodaciRepository studentPodaciRepository;
    private final StudentIndeksRepository studentIndeksRepository;
    private final StudijskiProgramRepository studijskiProgramRepository;
    private final StudentIndeksService studentIndeksService;
    private final StudentProfileService studentProfileService;
    private final UserAccountService userAccountService;
    private final EntityMappers entityMappers;

    @Transactional
    public Long addNewStudentPodaci(StudentPodaciRequest req) {
        String jmbg = clean(req.getJmbg());
        String fakultetskiEmail = clean(req.getFakultetskiEmail());
        String privatniEmail = clean(req.getPrivatniEmail());
        if (jmbg != null && studentPodaciRepository.existsByJmbg(jmbg)) {
            throw ApiException.conflict("STUDENT_JMBG_EXISTS", "Student sa ovim JMBG-om već postoji.");
        }
        if (fakultetskiEmail != null && studentPodaciRepository.existsByEmailFakultetskiIgnoreCase(fakultetskiEmail)) {
            throw ApiException.conflict("STUDENT_FACULTY_EMAIL_EXISTS", "Student sa ovim fakultetskim email-om već postoji.");
        }
        if (privatniEmail != null && studentPodaciRepository.existsByEmailPrivatniIgnoreCase(privatniEmail)) {
            throw ApiException.conflict("STUDENT_EMAIL_EXISTS", "Student sa ovim privatnim email-om već postoji.");
        }
        req.setJmbg(jmbg);
        req.setFakultetskiEmail(fakultetskiEmail);
        req.setPrivatniEmail(privatniEmail);
        StudentPodaci sp = studentPodaciRepository.save(Converters.toStudentPodaci(req));
        return sp.getId();
    }

    @Transactional
    public Long saveIndeks(StudentIndeksRequest req) {
        return saveIndeksProvision(req).getIndexId();
    }

    @Transactional
    public StudentIndexProvisionDTO saveIndeksProvision(StudentIndeksRequest req) {
        if (req.getStudent() == null || req.getStudent().getId() == null) {
            throw ApiException.badRequest("Student je obavezan za kreiranje indeksa.");
        }
        if (req.getGodina() < 2000 || req.getGodina() > 2100) {
            throw ApiException.badRequest("Godina indeksa mora biti između 2000 i 2100.");
        }
        if (req.getStudProgramOznaka() == null || req.getStudProgramOznaka().trim().isEmpty()) {
            throw ApiException.badRequest("Studijski program je obavezan.");
        }
        StudentPodaci sp = studentPodaciRepository.findById(req.getStudent().getId())
                .orElseThrow(() -> ApiException.notFound("Student ne postoji: " + req.getStudent().getId()));

        String programOznaka = req.getStudProgramOznaka().trim();
        var spList = studijskiProgramRepository.findByOznaka(programOznaka);
        if (spList.isEmpty()) {
            throw ApiException.notFound("Studijski program ne postoji: " + programOznaka);
        }

        DataIntegrityViolationException lastConflict = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            StudentIndeks si = Converters.toStudentIndeks(req);
            int next = studentIndeksService.allocateNextBrojForUpdate(req.getGodina(), programOznaka);
            si.setBroj(next);
            si.setStudProgramOznaka(programOznaka);
            si.setStudent(sp);
            si.setStudijskiProgram(spList.get(0));

            studentIndeksRepository.deactivateAllForStudent(sp.getId());
            si.setAktivan(true);

            try {
                StudentIndeks saved = studentIndeksRepository.saveAndFlush(si);
                UserAccountService.ProvisionResult provision = userAccountService.provisionStudentAccountWithCredential(sp, saved);
                return new StudentIndexProvisionDTO(saved.getId(), provision.getAccount().getUsername(),
                        provision.getTemporaryPassword(), provision.isCreated());
            } catch (DataIntegrityViolationException e) {
                lastConflict = e;
            }
        }
        throw ApiException.conflict("INDEX_ALLOCATION_CONFLICT", "Broj indeksa trenutno nije moguće dodeliti. Pokušajte ponovo.");
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    public List<StudentPodaciResponse> getAllStudentPodaci() {
        return studentPodaciRepository.findAll().stream()
                .map(entityMappers::fromStudentPodaciToResponse)
                .collect(Collectors.toList());
    }

    public Page<StudentPodaciResponse> getAllStudentPodaciPaginated(int page, int size) {
        return studentPodaciRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(entityMappers::fromStudentPodaciToResponse);
    }

    public StudentPodaciResponse getStudentPodaci(Long id) {
        return studentPodaciRepository.findById(id)
                .map(entityMappers::fromStudentPodaciToResponse)
                .orElse(null);
    }

    public StudentIndeksResponse getStudentIndeks(Long id) {
        return studentIndeksRepository.findById(id)
                .map(entityMappers::fromStudentIndexToResponse)
                .orElse(null);
    }

    public List<StudentIndeksResponse> getIndeksiForStudentPodaciId(Long id) {
        return studentIndeksRepository.findStudentIndeksiForStudentPodaciId(id).stream()
                .map(entityMappers::fromStudentIndexToResponse)
                .collect(Collectors.toList());
    }

    public StudentIndeksResponse fastSearch(String indeksShort) {
        String[] p = ParseUtils.parseIndeks(indeksShort);
        if (p == null) return null;
        String program = p[0];
        int godina = 2000 + Integer.parseInt(p[1]);
        int broj = Integer.parseInt(p[2]);
        StudentIndeks si = studentIndeksRepository.findStudentIndeks(program, godina, broj);
        return entityMappers.fromStudentIndexToResponse(si);
    }

    public StudentIndeksResponse emailSearch(String email) {
        String[] p = ParseUtils.parseEmail(email);
        if (p == null) return null;
        String program = p[0];
        int godina = 2000 + Integer.parseInt(p[1]);
        int broj = Integer.parseInt(p[2]);
        StudentIndeks si = studentIndeksRepository.findStudentIndeks(program, godina, broj);
        return entityMappers.fromStudentIndexToResponse(si);
    }

    public Page<StudentDTO> search(String ime, String prezime, String studProgram,
                                   Integer godina, Integer broj,
                                   int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String imeQ = (ime == null || ime.isBlank()) ? null : ime.trim();
        String prezimeQ = (prezime == null || prezime.isBlank()) ? null : prezime.trim();
        String studProgramQ = (studProgram == null || studProgram.isBlank()) ? null : studProgram.trim();

        Page<StudentPodaci> sp = studentPodaciRepository.searchAll(
                imeQ, prezimeQ, studProgramQ, godina, broj, pageable
        );
        return sp.map(entityMappers::fromStudentPodaciToDTO);
    }

    public Page<StudentDTO> globalSearch(String query, int page, int size) {
        if (query == null || query.trim().isEmpty()) return Page.empty(PageRequest.of(page, size));
        return studentPodaciRepository.globalSearch(query.trim(), PageRequest.of(page, size, Sort.by("id").descending()))
                .map(entityMappers::fromStudentPodaciToDTO);
    }

    public StudentProfileDTO getStudentProfile(Long id) {
        return studentProfileService.getStudentProfile(id);
    }

    public StudentWebProfileDTO getStudentWebProfile(Long id) {
        return studentProfileService.getStudentWebProfile(id);
    }

    public StudentWebProfileDTO getStudentWebProfileForEmail(String email) {
        String[] p = ParseUtils.parseEmail(email);
        if (p == null) return null;
        String program = p[0];
        int godina = 2000 + Integer.parseInt(p[1]);
        int broj = Integer.parseInt(p[2]);
        StudentIndeks si = studentIndeksRepository.findStudentIndeks(program, godina, broj);
        if (si == null) return null;
        return studentProfileService.getStudentWebProfile(si.getId());
    }

    public List<StudentPodaciResponse> findBySrednjaSkola(String naziv) {
        return studentPodaciRepository.findBySrednjaSkola(naziv).stream()
                .map(entityMappers::fromStudentPodaciToResponse)
                .collect(Collectors.toList());
    }
}
