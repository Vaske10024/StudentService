package org.raflab.studsluzba.e2e;

import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.StudentStatus;
import org.raflab.studsluzba.model.VrstaStudija;
import org.raflab.studsluzba.model.dtos.StudijskiProgramCreateRequest;
import org.raflab.studsluzba.model.dtos.UpisCreateRequest;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.IspitniRokRepository;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.raflab.studsluzba.repositories.RealizacijaPredmetaRepository;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.services.DrziPredmetAdminService;
import org.raflab.studsluzba.services.IspitAdminService;
import org.raflab.studsluzba.services.PredmetAdminService;
import org.raflab.studsluzba.services.RealizacijaPredmetaService;
import org.raflab.studsluzba.services.SkolskaGodinaService;
import org.raflab.studsluzba.services.StudijskiProgramAdminService;
import org.raflab.studsluzba.services.UpisObnovaService;
import org.raflab.studsluzba.services.UserAccountService;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class E2ETestDataFactory {
    private final EntityManager em;
    private final StudijskiProgramAdminService programService;
    private final PredmetAdminService predmetService;
    private final SkolskaGodinaService schoolYearService;
    private final RealizacijaPredmetaService realizationService;
    private final DrziPredmetAdminService assignmentService;
    private final IspitAdminService examService;
    private final UserAccountService accountService;
    private final UpisObnovaService enrollmentService;
    private final PasswordEncoder passwordEncoder;
    private final StudijskiProgramRepository programRepo;
    private final PredmetRepository predmetRepo;
    private final SkolskaGodinaRepository schoolYearRepo;
    private final RealizacijaPredmetaRepository realizationRepo;
    private final DrziPredmetRepository assignmentRepo;
    private final IspitRepository examRepo;
    private final IspitniRokRepository periodRepo;
    private final UserAccountRepository accountRepo;

    E2ETestDataFactory(EntityManager em,
                       StudijskiProgramAdminService programService,
                       PredmetAdminService predmetService,
                       SkolskaGodinaService schoolYearService,
                       RealizacijaPredmetaService realizationService,
                       DrziPredmetAdminService assignmentService,
                       IspitAdminService examService,
                       UserAccountService accountService,
                       UpisObnovaService enrollmentService,
                       PasswordEncoder passwordEncoder,
                       StudijskiProgramRepository programRepo,
                       PredmetRepository predmetRepo,
                       SkolskaGodinaRepository schoolYearRepo,
                       RealizacijaPredmetaRepository realizationRepo,
                       DrziPredmetRepository assignmentRepo,
                       IspitRepository examRepo,
                       IspitniRokRepository periodRepo,
                       UserAccountRepository accountRepo) {
        this.em = em;
        this.programService = programService;
        this.predmetService = predmetService;
        this.schoolYearService = schoolYearService;
        this.realizationService = realizationService;
        this.assignmentService = assignmentService;
        this.examService = examService;
        this.accountService = accountService;
        this.enrollmentService = enrollmentService;
        this.passwordEncoder = passwordEncoder;
        this.programRepo = programRepo;
        this.predmetRepo = predmetRepo;
        this.schoolYearRepo = schoolYearRepo;
        this.realizationRepo = realizationRepo;
        this.assignmentRepo = assignmentRepo;
        this.examRepo = examRepo;
        this.periodRepo = periodRepo;
        this.accountRepo = accountRepo;
    }

    Dataset createAcademicDataset() {
        Dataset data = new Dataset();
        data.prefix = "E2E_TEST_" + UUID.randomUUID().toString().substring(0, 8);

        VrstaStudija type = new VrstaStudija();
        type.setSkracenica(data.prefix + "_OAS");
        type.setPuniNaziv(data.prefix + " osnovne akademske studije");
        em.persist(type);
        em.flush();

        StudijskiProgramCreateRequest programRequest = new StudijskiProgramCreateRequest();
        programRequest.setOznaka(data.prefix);
        programRequest.setNaziv("E2E_TEST - Digitalno inzenjerstvo i AI");
        programRequest.setGodinaAkreditacije(2100);
        programRequest.setZvanje("Diplomirani inzenjer AI");
        programRequest.setTrajanjeGodina(4);
        programRequest.setUkupnoEspb(240);
        programRequest.setVrstaStudijaId(type.getId());
        Long programId = programService.create(programRequest);
        data.program = programRepo.findById(programId).orElseThrow(AssertionError::new);

        for (int i = 0; i < 5; i++) {
            String label = (2100 + i) + "/" + (2101 + i) + "-" + data.prefix;
            Long id = schoolYearService.create(label, i == 0);
            data.schoolYears.add(schoolYearRepo.findById(id).orElseThrow(AssertionError::new));
        }

        for (int year = 1; year <= 4; year++) {
            for (int ordinal = 1; ordinal <= 4; ordinal++) {
                String code = data.prefix + "_Y" + year + "S" + ordinal;
                Long subjectId = predmetService.create(
                        code,
                        "E2E predmet " + year + "." + ordinal,
                        "Izolovani lifecycle predmet",
                        15,
                        data.program.getId(),
                        true,
                        year,
                        ordinal <= 2 ? 1 : 2);
                Predmet subject = predmetRepo.findById(subjectId).orElseThrow(AssertionError::new);
                data.subjectsByYear.computeIfAbsent(year, ignored -> new ArrayList<>()).add(subject);
            }
        }

        Nastavnik professor = new Nastavnik();
        professor.setIme("E2E");
        professor.setPrezime("Profesor");
        professor.setSrednjeIme("Pipeline");
        professor.setEmail(data.prefix.toLowerCase() + ".professor@example.test");
        professor.setJmbg(data.prefix + "_PROF");
        em.persist(professor);
        em.flush();
        data.professor = professor;
        data.professorProvision = accountService.provisionProfessorAccountWithCredential(professor);

        data.admin = account("admin." + data.prefix.toLowerCase() + "@example.test", Role.ADMIN);

        for (int year = 1; year <= 4; year++) {
            SkolskaGodina schoolYear = data.schoolYears.get(year - 1);
            List<RealizacijaPredmeta> realizations = realizationService.ensureForEnrollment(
                    data.program.getId(), year, schoolYear.getId());
            for (RealizacijaPredmeta realization : realizations) {
                Long assignmentId = assignmentService.createOne(
                        null, professor.getId(), null, realization.getId(), DrziPredmet.Uloga.NOSILAC.name());
                DrziPredmet assignment = assignmentRepo.findById(assignmentId).orElseThrow(AssertionError::new);
                data.offerings.put(realization.getProgramPredmet().getPredmet().getId(),
                        new Offering(realization.getProgramPredmet().getPredmet(), realization, assignment));
            }
        }
        em.flush();
        return data;
    }

    StudentActor createStudent(Dataset data, String suffix, int number) {
        StudentPodaci personal = new StudentPodaci();
        personal.setIme("E2E");
        personal.setPrezime(suffix);
        personal.setSrednjeIme("Pipeline");
        personal.setJmbg(data.prefix + "_" + suffix + "_JMBG");
        personal.setEmailFakultetski(data.prefix.toLowerCase() + "." + suffix.toLowerCase() + "@student.example.test");
        personal.setEmailPrivatni(data.prefix.toLowerCase() + "." + suffix.toLowerCase() + "@private.example.test");
        em.persist(personal);

        StudentIndeks index = new StudentIndeks();
        index.setStudent(personal);
        index.setStudijskiProgram(data.program);
        index.setStudProgramOznaka(data.program.getOznaka());
        index.setGodina(2100);
        index.setBroj(number);
        index.setAktivan(true);
        index.setStatus(StudentStatus.AKTIVAN);
        index.setOstvarenoEspb(0);
        index.setVaziOd(LocalDate.now());
        em.persist(index);
        em.flush();

        UserAccountService.ProvisionResult provision = accountService.provisionStudentAccountWithCredential(personal, index);
        UpisCreateRequest request = new UpisCreateRequest();
        request.setIndeksId(index.getId());
        request.setUpisujeGodinu(1);
        Long enrollmentId = enrollmentService.upisi(request);
        em.flush();
        return new StudentActor(personal, index, provision, enrollmentId);
    }

    IspitniRok createPeriod(SkolskaGodina schoolYear, int offset) {
        LocalDate start = LocalDate.now().plusDays(10L + offset);
        IspitniRok period = new IspitniRok();
        period.setSkolskaGodina(schoolYearRepo.findById(schoolYear.getId()).orElseThrow(AssertionError::new));
        period.setDatumPocetka(start);
        period.setDatumZavrsetka(start.plusDays(7));
        period.setRegistrationStart(LocalDateTime.now().minusDays(2));
        period.setRegistrationEnd(LocalDateTime.now().plusDays(2));
        period.setCancellationEnd(LocalDateTime.now().plusDays(1));
        period.setActive(true);
        return periodRepo.save(period);
    }

    Ispit createExam(Offering offering, IspitniRok period, int dayOffset) {
        Long id = examService.create(period.getId(), offering.assignment.getId(),
                period.getDatumPocetka().plusDays(dayOffset), LocalTime.of(10, 0));
        return examRepo.findById(id).orElseThrow(AssertionError::new);
    }

    void activate(SkolskaGodina schoolYear) {
        schoolYearService.activate(schoolYear.getId());
        em.flush();
        em.clear();
    }

    private UserAccount account(String username, Role role) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode("E2e-Password-123!"));
        account.setRole(role);
        account.setEnabled(true);
        account.setMustChangePassword(false);
        return accountRepo.save(account);
    }

    static final class Dataset {
        String prefix;
        StudijskiProgram program;
        Nastavnik professor;
        UserAccount admin;
        UserAccountService.ProvisionResult professorProvision;
        final List<SkolskaGodina> schoolYears = new ArrayList<>();
        final Map<Integer, List<Predmet>> subjectsByYear = new LinkedHashMap<>();
        final Map<Long, Offering> offerings = new LinkedHashMap<>();

        Offering offering(Predmet subject) {
            return offerings.get(subject.getId());
        }
    }

    static final class StudentActor {
        final StudentPodaci personal;
        final StudentIndeks index;
        final UserAccountService.ProvisionResult provision;
        final Long firstEnrollmentId;

        StudentActor(StudentPodaci personal, StudentIndeks index,
                     UserAccountService.ProvisionResult provision, Long firstEnrollmentId) {
            this.personal = personal;
            this.index = index;
            this.provision = provision;
            this.firstEnrollmentId = firstEnrollmentId;
        }
    }

    static final class Offering {
        final Predmet subject;
        final RealizacijaPredmeta realization;
        final DrziPredmet assignment;

        Offering(Predmet subject, RealizacijaPredmeta realization, DrziPredmet assignment) {
            this.subject = subject;
            this.realization = realization;
            this.assignment = assignment;
        }
    }
}
