package org.raflab.studsluzba.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raflab.studsluzba.model.*;
import org.raflab.studsluzba.model.documents.*;
import org.raflab.studsluzba.model.enrollment.*;
import org.raflab.studsluzba.model.finance.*;
import org.raflab.studsluzba.model.ispiti.*;
import org.raflab.studsluzba.model.notification.Notification;
import org.raflab.studsluzba.model.schedule.*;
import org.raflab.studsluzba.model.security.*;
import org.raflab.studsluzba.repositories.security.SystemSettingRepository;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Component
@Profile("dev & !prod & !e2e")
@ConditionalOnProperty(name = "app.seed.demo-data.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class DevDemoDataInitializer implements ApplicationRunner {
    public static final String PASSWORD = "DemoPass123!";
    private static final String MARKER = "dev.demo.seed.version";
    private static final String VERSION = "2026-06-full-workflow-v1";

    private final EntityManager em;
    private final UserAccountRepository accountRepo;
    private final SystemSettingRepository settingRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (settingRepo.findBySettingKey(MARKER).map(SystemSetting::getSettingValue).filter(VERSION::equals).isPresent()) {
            log.info("DEV demo data is already present ({}).", VERSION);
            return;
        }

        log.warn("Creating DEV-only demo dataset. This initializer must never be enabled in production.");
        Seed seed = new Seed();
        seed.admin = account("admin@demo.edu", Role.ADMIN, null, null, null);
        seed.program = program();
        seed.activeYear = schoolYear("2025/26", true);
        seed.nextYear = schoolYear("2026/27", false);
        seed.subjects = curriculum(seed.program);
        seed.realizations = realizations(seed.subjects, seed.activeYear);
        realizations(seed.subjects, seed.nextYear);
        seed.professors = professors();
        seed.assignments = assignments(seed);
        seed.students = students(seed);
        enrollmentsAndListening(seed);
        seed.periods = periods(seed.activeYear);
        seed.exams = exams(seed);
        preExamWork(seed);
        examHistory(seed);
        finance(seed);
        yearEnrollmentRequests(seed);
        documentsAndApplications(seed);
        schedule(seed);
        notifications(seed);

        SystemSetting marker = settingRepo.findBySettingKey(MARKER).orElseGet(SystemSetting::new);
        marker.setSettingKey(MARKER);
        marker.setSettingValue(VERSION);
        marker.setDescription("Marks a complete DEV-only demo data seed.");
        marker.setUpdatedByUserId(seed.admin.getId());
        settingRepo.save(marker);
        log.warn("DEV demo dataset created. Demo password: {}", PASSWORD);
    }

    private StudijskiProgram program() {
        VrstaStudija type = one("select v from VrstaStudija v where lower(v.skracenica)=lower(:value)", "value", "OAS");
        if (type == null) {
            type = new VrstaStudija();
            type.setSkracenica("OAS");
            type.setPuniNaziv("Osnovne akademske studije");
            em.persist(type);
        }
        StudijskiProgram program = one("select p from StudijskiProgram p where p.oznaka=:value and p.godinaAkreditacije=2025",
                "value", "SI");
        if (program == null) {
            program = new StudijskiProgram();
            program.setOznaka("SI");
            program.setNaziv("Softversko inzenjerstvo");
            program.setGodinaAkreditacije(2025);
            program.setZvanje("Diplomirani inzenjer softvera");
            program.setTrajanjeGodina(4);
            program.setTrajanjeSemestara(8);
            program.setUkupnoEspb(240);
            program.setVrstaStudija(type);
            em.persist(program);
        }
        return program;
    }

    private SkolskaGodina schoolYear(String label, boolean active) {
        SkolskaGodina year = one("select s from SkolskaGodina s where s.godina=:value", "value", label);
        if (year == null) {
            year = new SkolskaGodina();
            year.setGodina(label);
            em.persist(year);
        }
        if (active) {
            em.createQuery("update SkolskaGodina s set s.aktivna=false where s.aktivna=true and s<>:year")
                    .setParameter("year", year).executeUpdate();
        }
        year.setAktivna(active);
        return year;
    }

    private Map<String, Predmet> curriculum(StudijskiProgram program) {
        String[][] rows = {
                {"SI101","Programiranje 1","1","1"},{"SI102","Matematika 1","1","1"},{"SI103","Uvod u racunarstvo","1","1"},
                {"SI104","Arhitektura racunara","1","1"},{"SI105","Engleski jezik 1","1","1"},
                {"SI106","Programiranje 2","1","2"},{"SI107","Matematika 2","1","2"},{"SI108","Objektno orijentisano programiranje","1","2"},
                {"SI109","Baze podataka 1","1","2"},{"SI110","Web programiranje","1","2"},
                {"SI201","Algoritmi i strukture podataka","2","1"},{"SI202","Operativni sistemi","2","1"},{"SI203","Softversko inzenjerstvo","2","1"},
                {"SI204","Racunarske mreze","2","1"},{"SI205","Baze podataka 2","2","1"},
                {"SI206","Java aplikacije","2","2"},{"SI207","Paralelno programiranje","2","2"},{"SI208","Razvoj web aplikacija","2","2"},
                {"SI209","UML i projektovanje softvera","2","2"},{"SI210","Verifikacija i validacija softvera","2","2"},
                {"SI301","Distribuirani sistemi","3","1"},{"SI302","Bezbednost softvera","3","1"},{"SI303","Cloud computing","3","1"},
                {"SI304","Masinsko ucenje","3","1"},{"SI305","Mobilne aplikacije","3","1"},
                {"SI306","Mikroservisi","3","2"},{"SI307","DevOps","3","2"},{"SI308","Napredne baze podataka","3","2"},
                {"SI309","Projektovanje informacionih sistema","3","2"},{"SI310","Strucna praksa","3","2"},
                {"SI401","Diplomski projekat 1","4","1"},{"SI402","Upravljanje softverskim projektima","4","1"},{"SI403","Vestacka inteligencija","4","1"},
                {"SI404","Informaciona bezbednost","4","1"},{"SI405","Preduzetnistvo u IT","4","1"},
                {"SI406","Diplomski projekat 2","4","2"},{"SI407","Big Data sistemi","4","2"},{"SI408","Napredni web sistemi","4","2"},
                {"SI409","Izborni predmet 1","4","2"},{"SI410","Izborni predmet 2","4","2"}
        };
        Map<String, Predmet> result = new LinkedHashMap<>();
        for (String[] row : rows) {
            Predmet subject = one("select p from Predmet p where p.sifra=:value", "value", row[0]);
            if (subject == null) {
                subject = new Predmet();
                subject.setSifra(row[0]);
                subject.setNaziv(row[1]);
                subject.setOpis("Demo predmet programa Softversko inzenjerstvo");
                subject.setEspb(6);
                subject.setObavezan(!row[0].equals("SI409") && !row[0].equals("SI410"));
                subject.setStudProgram(program);
                em.persist(subject);
            }
            ProgramPredmet link = one("select pp from ProgramPredmet pp where pp.program=:program and pp.predmet=:subject",
                    "program", program, "subject", subject);
            if (link == null) {
                link = new ProgramPredmet();
                link.setProgram(program);
                link.setPredmet(subject);
                link.setGodinaStudija(Integer.parseInt(row[2]));
                link.setSemestarUGodini(Integer.parseInt(row[3]));
                link.setFondPredavanja(3);
                link.setFondVezbi(2);
                link.setFondPraktikum(row[0].endsWith("10") || row[0].startsWith("SI4") ? 1 : 0);
                em.persist(link);
            }
            result.put(row[0], subject);
        }
        return result;
    }

    private Map<String, RealizacijaPredmeta> realizations(Map<String, Predmet> subjects, SkolskaGodina year) {
        Map<String, RealizacijaPredmeta> result = new LinkedHashMap<>();
        for (Predmet subject : subjects.values()) {
            ProgramPredmet link = one("select pp from ProgramPredmet pp where pp.predmet=:subject", "subject", subject);
            RealizacijaPredmeta realization = one(
                    "select r from RealizacijaPredmeta r where r.programPredmet=:link and r.skolskaGodina=:year",
                    "link", link, "year", year);
            if (realization == null) {
                realization = new RealizacijaPredmeta();
                realization.setProgramPredmet(link);
                realization.setSkolskaGodina(year);
                realization.setStatus(RealizacijaPredmeta.Status.ACTIVE);
                em.persist(realization);
            }
            result.put(subject.getSifra(), realization);
        }
        return result;
    }

    private List<Nastavnik> professors() {
        return Arrays.asList(
                professor("Marko", "Aleksic", "marko.aleksic@demo.edu", "DEMO-PROF-001"),
                professor("Jelena", "Jovanovic", "jelena.jovanovic@demo.edu", "DEMO-PROF-002"),
                professor("Nikola", "Petrovic", "nikola.petrovic@demo.edu", "DEMO-PROF-003"));
    }

    private Nastavnik professor(String first, String last, String email, String jmbg) {
        Nastavnik professor = one("select n from Nastavnik n where lower(n.email)=lower(:value)", "value", email);
        if (professor == null) {
            professor = new Nastavnik();
            professor.setIme(first);
            professor.setPrezime(last);
            professor.setSrednjeIme("Demo");
            professor.setEmail(email);
            professor.setJmbg(jmbg);
            professor.setBrojTelefona("+381 60 555 10" + jmbg.substring(jmbg.length() - 1));
            professor.setZavrseniFakultet("Univerzitet Demo");
            em.persist(professor);
        }
        account(email, Role.PROFESSOR, null, professor, null);
        return professor;
    }

    private Map<String, DrziPredmet> assignments(Seed seed) {
        Map<String, DrziPredmet> result = new LinkedHashMap<>();
        int index = 0;
        for (Map.Entry<String, RealizacijaPredmeta> entry : seed.realizations.entrySet()) {
            Nastavnik owner = seed.professors.get(index++ % seed.professors.size());
            DrziPredmet assignment = assignment(entry.getValue(), owner, DrziPredmet.Uloga.NOSILAC);
            result.put(entry.getKey(), assignment);
            if (index <= 10) {
                assignment(entry.getValue(), seed.professors.get(index % seed.professors.size()), DrziPredmet.Uloga.VEZBE);
            }
        }
        return result;
    }

    private DrziPredmet assignment(RealizacijaPredmeta realization, Nastavnik professor, DrziPredmet.Uloga role) {
        DrziPredmet assignment = one("select d from DrziPredmet d where d.realizacijaPredmeta=:r and d.nastavnik=:n and d.uloga=:u",
                "r", realization, "n", professor, "u", role);
        if (assignment == null) {
            assignment = new DrziPredmet();
            assignment.setRealizacijaPredmeta(realization);
            assignment.setPredmet(realization.getProgramPredmet().getPredmet());
            assignment.setSkolskaGodina(realization.getSkolskaGodina());
            assignment.setNastavnik(professor);
            assignment.setUloga(role);
            em.persist(assignment);
        }
        return assignment;
    }

    private Map<String, StudentIndeks> students(Seed seed) {
        Map<String, StudentIndeks> result = new LinkedHashMap<>();
        result.put("freshman", student(seed.program, "student.freshman@demo.edu", "Ana", "Jovic", 1, 1));
        result.put("good", student(seed.program, "student.good@demo.edu", "Luka", "Markovic", 2, 2));
        result.put("conditional", student(seed.program, "student.conditional@demo.edu", "Mina", "Ilic", 3, 1));
        result.put("renewal", student(seed.program, "student.renewal@demo.edu", "Petar", "Simic", 4, 1));
        result.put("debt", student(seed.program, "student.debt@demo.edu", "Sara", "Nikolic", 5, 1));
        result.put("pendingrequest", student(seed.program, "student.pendingrequest@demo.edu", "Vuk", "Pavlovic", 6, 1));
        return result;
    }

    private StudentIndeks student(StudijskiProgram program, String email, String first, String last, int number, int year) {
        UserAccount existing = accountRepo.findByUsername(email).orElse(null);
        if (existing != null && existing.getLinkedStudentIndeks() != null) return existing.getLinkedStudentIndeks();
        StudentPodaci personal = one("select s from StudentPodaci s where lower(s.emailFakultetski)=lower(:value)", "value", email);
        if (personal == null) {
            personal = new StudentPodaci();
            personal.setIme(first);
            personal.setPrezime(last);
            personal.setSrednjeIme("Demo");
            personal.setJmbg("DEMO-STUDENT-00" + number);
            personal.setEmailFakultetski(email);
            personal.setEmailPrivatni(email.replace("@demo.edu", ".private@demo.edu"));
            personal.setDatumRodjenja(LocalDate.of(2004 - year, Month.MARCH, Math.min(28, 10 + number)));
            personal.setMestoRodjenja("Beograd");
            personal.setMestoPrebivalista("Beograd");
            personal.setAdresa("Studentska " + number);
            personal.setBrojTelefonaMobilni("+381 64 555 20" + number);
            em.persist(personal);
        }
        StudentIndeks indeks = one("select i from StudentIndeks i where i.godina=2025 and i.broj=:number and i.studProgramOznaka='SI'",
                "number", number);
        if (indeks == null) {
            indeks = new StudentIndeks();
            indeks.setStudent(personal);
            indeks.setStudijskiProgram(program);
            indeks.setStudProgramOznaka("SI");
            indeks.setGodina(2025);
            indeks.setBroj(number);
            indeks.setAktivan(true);
            indeks.setStatus(StudentStatus.AKTIVAN);
            indeks.setVaziOd(LocalDate.now().minusMonths(9));
            indeks.setActivatedAt(LocalDateTime.now().minusMonths(9));
            indeks.setNacinFinansiranja(number == 5 ? "SAMOFINANSIRANJE" : "BUDZET");
            indeks.setOstvarenoEspb(0);
            em.persist(indeks);
        }
        account(email, Role.STUDENT, indeks, null, personal);
        return indeks;
    }

    private void enrollmentsAndListening(Seed seed) {
        enroll(seed.students.get("freshman"), 1, seed.activeYear, seed, 1);
        enrollmentOnly(seed.students.get("good"), 1, seed.activeYear);
        enroll(seed.students.get("good"), 2, seed.activeYear, seed, 2);
        enroll(seed.students.get("conditional"), 1, seed.activeYear, seed, 1);
        enroll(seed.students.get("renewal"), 1, seed.activeYear, seed, 1);
        enroll(seed.students.get("debt"), 1, seed.activeYear, seed, 1);
        enroll(seed.students.get("pendingrequest"), 1, seed.activeYear, seed, 1);
    }

    private UpisGodine enrollmentOnly(StudentIndeks student, int enrolledYear, SkolskaGodina schoolYear) {
        UpisGodine enrollment = one("select u from UpisGodine u where u.indeks=:student and u.upisujeGodinu=:studyYear and u.skolskaGodina=:schoolYear",
                "student", student, "studyYear", enrolledYear, "schoolYear", schoolYear);
        if (enrollment == null) {
            enrollment = new UpisGodine();
            enrollment.setIndeks(student);
            enrollment.setUpisujeGodinu(enrolledYear);
            enrollment.setSkolskaGodina(schoolYear);
            enrollment.setDatum(LocalDate.now().minusMonths(10));
            enrollment.setNapomena("DEV demo istorijski upis");
            em.persist(enrollment);
        }
        return enrollment;
    }

    private UpisGodine enroll(StudentIndeks student, int enrolledYear, SkolskaGodina schoolYear, Seed seed, int listenYear) {
        UpisGodine enrollment = one("select u from UpisGodine u where u.indeks=:student and u.upisujeGodinu=:studyYear and u.skolskaGodina=:schoolYear",
                "student", student, "studyYear", enrolledYear, "schoolYear", schoolYear);
        if (enrollment == null) {
            enrollment = new UpisGodine();
            enrollment.setIndeks(student);
            enrollment.setUpisujeGodinu(enrolledYear);
            enrollment.setSkolskaGodina(schoolYear);
            enrollment.setDatum(LocalDate.now().minusMonths(enrolledYear == 1 ? 8 : 6));
            enrollment.setNapomena("DEV demo upis");
            em.persist(enrollment);
        }
        for (RealizacijaPredmeta realization : seed.realizations.values()) {
            if (realization.getProgramPredmet().getGodinaStudija() != listenYear) continue;
            SlusaPredmet listening = one("select s from SlusaPredmet s where s.studentIndeks=:student and s.realizacijaPredmeta=:realization",
                    "student", student, "realization", realization);
            if (listening == null) {
                listening = new SlusaPredmet();
                listening.setStudentIndeks(student);
                listening.setRealizacijaPredmeta(realization);
                listening.setDrziPredmet(seed.assignments.get(realization.getProgramPredmet().getPredmet().getSifra()));
                listening.setSkolskaGodina(schoolYear);
                listening.setUpisGodine(enrollment);
                em.persist(listening);
            }
        }
        return enrollment;
    }

    private List<IspitniRok> periods(SkolskaGodina year) {
        LocalDate today = LocalDate.now();
        return Arrays.asList(
                period(year, today.minusDays(50), today.minusDays(42), today.minusDays(70).atStartOfDay(),
                        today.minusDays(55).atTime(23, 59), today.minusDays(56).atTime(23, 59), false),
                period(year, today.plusDays(7), today.plusDays(18), LocalDateTime.now().minusDays(3),
                        LocalDateTime.now().plusDays(4), LocalDateTime.now().plusDays(2), true),
                period(year, today.plusDays(45), today.plusDays(55), LocalDateTime.now().plusDays(25),
                        LocalDateTime.now().plusDays(38), LocalDateTime.now().plusDays(35), true));
    }

    private IspitniRok period(SkolskaGodina year, LocalDate start, LocalDate end, LocalDateTime registrationStart,
                              LocalDateTime registrationEnd, LocalDateTime cancellationEnd, boolean active) {
        IspitniRok period = one("select r from IspitniRok r where r.skolskaGodina=:year and r.datumPocetka=:start and r.datumZavrsetka=:end",
                "year", year, "start", start, "end", end);
        if (period == null) {
            period = new IspitniRok();
            period.setSkolskaGodina(year);
            period.setDatumPocetka(start);
            period.setDatumZavrsetka(end);
            period.setRegistrationStart(registrationStart);
            period.setRegistrationEnd(registrationEnd);
            period.setCancellationEnd(cancellationEnd);
            period.setActive(active);
            em.persist(period);
        }
        return period;
    }

    private Map<String, Ispit> exams(Seed seed) {
        Map<String, Ispit> result = new LinkedHashMap<>();
        int i = 0;
        for (String code : seed.subjects.keySet()) {
            if (i >= 20) break;
            result.put("past-" + code, exam(seed.assignments.get(code), seed.periods.get(0),
                    seed.periods.get(0).getDatumPocetka().plusDays(i % 8), true));
            result.put("active-" + code, exam(seed.assignments.get(code), seed.periods.get(1),
                    seed.periods.get(1).getDatumPocetka().plusDays(i % 10), false));
            if (i < 10) result.put("future-" + code, exam(seed.assignments.get(code), seed.periods.get(2),
                    seed.periods.get(2).getDatumPocetka().plusDays(i), false));
            i++;
        }
        return result;
    }

    private Ispit exam(DrziPredmet assignment, IspitniRok period, LocalDate date, boolean locked) {
        Ispit exam = one("select i from Ispit i where i.drziPredmet=:assignment and i.datumOdrzavanja=:date",
                "assignment", assignment, "date", date);
        if (exam == null) {
            exam = new Ispit();
            exam.setDrziPredmet(assignment);
            exam.setNastavnik(assignment.getNastavnik());
            exam.setPredmet(assignment.getPredmet());
            exam.setIspitniRok(period);
            exam.setDatumOdrzavanja(date);
            exam.setVremePocetka(LocalTime.of(9 + (assignment.getPredmet().getSifra().hashCode() & 3), 0));
            exam.setZakljucen(locked);
            em.persist(exam);
        }
        return exam;
    }

    private void preExamWork(Seed seed) {
        for (String code : seed.subjects.keySet()) {
            Predmet subject = seed.subjects.get(code);
            preExam(subject, seed.activeYear, "Kolokvijum 1", 10);
            preExam(subject, seed.activeYear, "Kolokvijum 2", 10);
            preExam(subject, seed.activeYear, "Projekat", 10);
        }
        for (Map.Entry<String, StudentIndeks> student : seed.students.entrySet()) {
            int studyYear = student.getKey().equals("good") ? 2 : 1;
            for (Predmet subject : seed.subjects.values()) {
                ProgramPredmet link = one("select pp from ProgramPredmet pp where pp.predmet=:subject", "subject", subject);
                if (link.getGodinaStudija() != studyYear) continue;
                List<PredispitnaObaveza> obligations = this.<PredispitnaObaveza>list(
                        "select p from PredispitnaObaveza p where p.predmet=:subject and p.skolskaGodina=:year",
                        "subject", subject, "year", seed.activeYear);
                for (PredispitnaObaveza obligation : obligations) {
                    int points = student.getKey().equals("renewal") ? 4 : student.getKey().equals("debt") ? 7 : 9;
                    preExamScore(student.getValue(), obligation, points);
                }
            }
        }
    }

    private PredispitnaObaveza preExam(Predmet subject, SkolskaGodina year, String type, int max) {
        PredispitnaObaveza obligation = one(
                "select p from PredispitnaObaveza p where p.predmet=:subject and p.skolskaGodina=:year and p.vrsta=:type",
                "subject", subject, "year", year, "type", type);
        if (obligation == null) {
            obligation = new PredispitnaObaveza();
            obligation.setPredmet(subject);
            obligation.setSkolskaGodina(year);
            obligation.setVrsta(type);
            obligation.setMaxPoeni(max);
            em.persist(obligation);
        }
        return obligation;
    }

    private void preExamScore(StudentIndeks student, PredispitnaObaveza obligation, int points) {
        OstvarenaPredObav score = one("select o from OstvarenaPredObav o where o.student=:student and o.obaveza=:obligation",
                "student", student, "obligation", obligation);
        if (score == null) {
            score = new OstvarenaPredObav();
            score.setStudent(student);
            score.setObaveza(obligation);
            score.setOsvojeniPoeni(points);
            em.persist(score);
        }
    }

    private void examHistory(Seed seed) {
        passSubjects(seed, "good", 16, new int[]{8, 9, 10});
        passSubjects(seed, "conditional", 7, new int[]{8, 9});
        passSubjects(seed, "renewal", 3, new int[]{6, 7});
        passSubjects(seed, "pendingrequest", 8, new int[]{8, 9, 10});
        registration(seed.students.get("good"), seed.exams.get("past-SI209"), PrijavaStatus.PAO, 5, 46, true, false);
        registration(seed.students.get("conditional"), seed.exams.get("past-SI108"), PrijavaStatus.ODSUTAN, 5, null, false, false);
        registration(seed.students.get("freshman"), seed.exams.get("active-SI101"), PrijavaStatus.PRIJAVLJEN, 0, null, false, false);
        registration(seed.students.get("freshman"), seed.exams.get("active-SI103"), PrijavaStatus.PRIJAVLJEN, 0, null, false, false);
        registration(seed.students.get("conditional"), seed.exams.get("active-SI108"), PrijavaStatus.PRIJAVLJEN, 0, null, false, false);
        registration(seed.students.get("renewal"), seed.exams.get("active-SI104"), PrijavaStatus.PRIJAVLJEN, 0, null, false, false);
        registration(seed.students.get("freshman"), seed.exams.get("past-SI102"), PrijavaStatus.ODJAVLJEN, 0, null, false, false);
        seed.students.values().forEach(s -> s.setOstvarenoEspb(earnedEcts(s)));
    }

    private void passSubjects(Seed seed, String studentKey, int count, int[] grades) {
        int i = 0;
        for (String code : seed.subjects.keySet()) {
            if (i >= count) break;
            int grade = grades[i % grades.length];
            int total = grade == 8 ? 75 : grade == 9 ? 85 : grade == 10 ? 95 : grade == 7 ? 65 : 55;
            registration(seed.students.get(studentKey), seed.exams.get("past-" + code), PrijavaStatus.POLOZIO,
                    grade, total - 27, true, false);
            i++;
        }
    }

    private PrijavaIspita registration(StudentIndeks student, Ispit exam, PrijavaStatus status, int grade,
                                        Integer examPoints, boolean attended, boolean voided) {
        PrijavaIspita attempt = one("select p from PrijavaIspita p where p.student=:student and p.ispit=:exam",
                "student", student, "exam", exam);
        if (attempt == null) {
            attempt = new PrijavaIspita();
            attempt.setStudent(student);
            attempt.setIspit(exam);
            attempt.setPredmet(exam.getPredmet());
            attempt.setDatumPrijave(exam.getDatumOdrzavanja().minusDays(20));
            attempt.setStatus(status);
            attempt.setOcena(grade);
            attempt.setBrojOsvojenihPoena(examPoints);
            attempt.setDaLiJeIzasao(attended);
            attempt.setPonisteno(voided);
            if (status == PrijavaStatus.ODJAVLJEN) {
                attempt.setCancelledAt(exam.getDatumOdrzavanja().minusDays(10).atStartOfDay());
                attempt.setCancellationReason("Student je izabrao drugi termin.");
            }
            em.persist(attempt);
        }
        return attempt;
    }

    private int earnedEcts(StudentIndeks student) {
        Long value = em.createQuery("select count(distinct p.predmet.id) from PrijavaIspita p where p.student=:student " +
                        "and p.ponisteno=false and p.ocena>=6 and (p.daLiJeIzasao=true or p.priznatSDrugogFakulteta=true)", Long.class)
                .setParameter("student", student).getSingleResult();
        return value.intValue() * 6;
    }

    private void finance(Seed seed) {
        financialScenario(seed.students.get("freshman"), new BigDecimal("150.00"), new BigDecimal("150.00"), "Administrativna naknada");
        financialScenario(seed.students.get("good"), new BigDecimal("1000.00"), new BigDecimal("1000.00"), "Skolarina - placeno");
        financialScenario(seed.students.get("conditional"), new BigDecimal("1200.00"), new BigDecimal("900.00"), "Delimicno placena skolarina");
        financialScenario(seed.students.get("renewal"), new BigDecimal("600.00"), new BigDecimal("650.00"), "Obnova godine - preplata");
        financialScenario(seed.students.get("debt"), new BigDecimal("1800.00"), new BigDecimal("300.00"), "Skolarina - dug blokira prijavu");
        financialScenario(seed.students.get("pendingrequest"), new BigDecimal("500.00"), new BigDecimal("500.00"), "Naknada za upis");
    }

    private void financialScenario(StudentIndeks student, BigDecimal charge, BigDecimal payment, String description) {
        Long count = em.createQuery("select count(l) from LedgerEntry l where l.studentIndeks=:student", Long.class)
                .setParameter("student", student).getSingleResult();
        if (count > 0) return;
        TuitionPlan plan = new TuitionPlan();
        plan.setStudentIndeks(student);
        plan.setFinancingType(student.getNacinFinansiranja().equals("BUDZET") ? FinancingType.BUDGET : FinancingType.SELF_FINANCED);
        plan.setTotalEur(charge);
        plan.setLocked(true);
        em.persist(plan);
        FinancialObligation obligation = new FinancialObligation();
        obligation.setStudentIndeks(student);
        obligation.setTuitionPlan(plan);
        obligation.setType("TUITION");
        obligation.setAmountEur(charge);
        obligation.setAllocatedEur(payment.min(charge));
        obligation.setDueDate(LocalDate.now().minusDays(30));
        em.persist(obligation);
        LedgerEntry chargeEntry = ledger(student, LedgerEntry.Type.CHARGE, charge, description);
        LedgerEntry paymentEntry = ledger(student, LedgerEntry.Type.PAYMENT, payment.negate(), "Demo uplata");
        if (payment.signum() > 0) {
            PaymentAllocation allocation = new PaymentAllocation();
            allocation.setPayment(paymentEntry);
            allocation.setObligation(obligation);
            allocation.setAmountEur(payment.min(charge));
            em.persist(allocation);
            Uplata legacyPayment = new Uplata();
            legacyPayment.setIndeks(student);
            legacyPayment.setDatum(LocalDate.now().minusDays(15));
            legacyPayment.setIznosRsd(payment.multiply(new BigDecimal("117.00")));
            legacyPayment.setSrednjiKursEur(new BigDecimal("117.000000"));
            legacyPayment.setFallbackKurs(false);
            em.persist(legacyPayment);
        }
    }

    private LedgerEntry ledger(StudentIndeks student, LedgerEntry.Type type, BigDecimal amount, String description) {
        LedgerEntry entry = new LedgerEntry();
        entry.setStudentIndeks(student);
        entry.setType(type);
        entry.setAmountEur(amount);
        entry.setDescription(description);
        em.persist(entry);
        return entry;
    }

    private void yearEnrollmentRequests(Seed seed) {
        yearRequest(seed, "conditional", StudyYearEnrollmentRequest.Type.CONDITIONAL_ENROLLMENT,
                StudyYearEnrollmentRequest.Status.PENDING_DOCUMENTS, false, false, false, "SI108", "SI109", "SI110");
        yearRequest(seed, "pendingrequest", StudyYearEnrollmentRequest.Type.ENROLL_NEXT_YEAR,
                StudyYearEnrollmentRequest.Status.PENDING_ADMIN_APPROVAL, true, true, true, "SI109", "SI110");
        yearRequest(seed, "good", StudyYearEnrollmentRequest.Type.ENROLL_NEXT_YEAR,
                StudyYearEnrollmentRequest.Status.APPROVED, true, true, true, "SI207", "SI208", "SI209", "SI210");
        yearRequest(seed, "renewal", StudyYearEnrollmentRequest.Type.RENEW_YEAR,
                StudyYearEnrollmentRequest.Status.NEEDS_CHANGES, true, false, false, "SI104", "SI105", "SI106");
        yearRequest(seed, "debt", StudyYearEnrollmentRequest.Type.RENEW_YEAR,
                StudyYearEnrollmentRequest.Status.REJECTED, true, false, true, "SI101", "SI102");
    }

    private StudyYearEnrollmentRequest yearRequest(Seed seed, String studentKey, StudyYearEnrollmentRequest.Type type,
                                                   StudyYearEnrollmentRequest.Status status, boolean contract,
                                                   boolean payment, boolean docs, String... transferredCodes) {
        StudentIndeks student = seed.students.get(studentKey);
        StudyYearEnrollmentRequest request = one(
                "select r from StudyYearEnrollmentRequest r where r.studentIndeks=:student and r.targetSchoolYear=:target and r.type=:type",
                "student", student, "target", seed.nextYear, "type", type);
        if (request != null) return request;
        request = new StudyYearEnrollmentRequest();
        request.setStudentIndeks(student);
        request.setCurrentSchoolYear(seed.activeYear);
        request.setTargetSchoolYear(seed.nextYear);
        request.setType(type);
        request.setStatus(status);
        request.setCurrentStudyYear(studentKey.equals("good") ? 2 : 1);
        request.setRequestedStudyYear(type == StudyYearEnrollmentRequest.Type.RENEW_YEAR ? request.getCurrentStudyYear() : request.getCurrentStudyYear() + 1);
        request.setEarnedEctsSnapshot(earnedEcts(student));
        request.setContractReceived(contract);
        request.setPaymentConfirmed(payment);
        request.setDocumentationComplete(docs);
        request.setStudentNote("DEV demo zahtev za rucno testiranje.");
        request.setAdminNote(status == StudyYearEnrollmentRequest.Status.NEEDS_CHANGES ? "Dostaviti potvrdu o uplati." :
                status == StudyYearEnrollmentRequest.Status.REJECTED ? "Zahtev odbijen zbog nepodmirenog duga." : null);
        request.setSubmittedByUserId(accountRepo.findByLinkedStudentIndeksId(student.getId()).map(UserAccount::getId).orElse(null));
        if (status == StudyYearEnrollmentRequest.Status.APPROVED || status == StudyYearEnrollmentRequest.Status.REJECTED) {
            request.setDecidedByUserId(seed.admin.getId());
            request.setDecidedAt(LocalDateTime.now().minusDays(2));
        }
        em.persist(request);
        for (String code : transferredCodes) {
            StudyYearEnrollmentTransferredSubject item = new StudyYearEnrollmentTransferredSubject();
            item.setRequest(request);
            item.setSubject(seed.subjects.get(code));
            item.setEctsSnapshot(6);
            request.getTransferredSubjects().add(item);
        }
        StudyYearEnrollmentRequestHistory history = new StudyYearEnrollmentRequestHistory();
        history.setRequest(request);
        history.setNewStatus(status);
        history.setNote("DEV demo istorija statusa.");
        history.setActorUserId(seed.admin.getId());
        em.persist(history);
        return request;
    }

    private void documentsAndApplications(Seed seed) {
        studentRequest(seed, "freshman", RequestType.POTVRDA_O_STUDIRANJU, StudentRequest.Status.SUBMITTED);
        studentRequest(seed, "good", RequestType.UVERENJE_O_POLOZENIM_ISPITIMA, StudentRequest.Status.APPROVED);
        studentRequest(seed, "conditional", RequestType.PROMENA_LICNIH_PODATAKA, StudentRequest.Status.REJECTED);
        studentRequest(seed, "pendingrequest", RequestType.POTVRDA_O_STUDIRANJU, StudentRequest.Status.IN_REVIEW);
        enrollmentApplication(seed, "demo-enrollment-submitted", EnrollmentApplication.Status.SUBMITTED, "Ivana", "Demo", "ivana.demo@candidate.edu");
        enrollmentApplication(seed, "demo-enrollment-rejected", EnrollmentApplication.Status.REJECTED, "Milan", "Demo", "milan.demo@candidate.edu");
    }

    private StudentRequest studentRequest(Seed seed, String key, RequestType type, StudentRequest.Status status) {
        StudentIndeks student = seed.students.get(key);
        StudentRequest request = one("select r from StudentRequest r where r.studentIndeks=:student and r.type=:type",
                "student", student, "type", type);
        if (request != null) return request;
        request = new StudentRequest();
        request.setStudentIndeks(student);
        request.setType(type);
        request.setStatus(status);
        request.setReason("DEV demo zahtev za dokument.");
        request.setSubmittedByUserId(accountRepo.findByLinkedStudentIndeksId(student.getId()).map(UserAccount::getId).orElse(null));
        if (status == StudentRequest.Status.APPROVED || status == StudentRequest.Status.REJECTED) {
            request.setDecidedByUserId(seed.admin.getId());
            request.setDecidedAt(LocalDateTime.now().minusDays(3));
            request.setDecisionNote(status == StudentRequest.Status.APPROVED ? "Dokument je spreman." : "Potrebna je dopuna.");
        }
        em.persist(request);
        StudentRequestStatusHistory history = new StudentRequestStatusHistory();
        history.setStudentRequest(request);
        history.setOldStatus("SUBMITTED");
        history.setNewStatus(status.name());
        history.setNote("DEV demo obrada zahteva.");
        history.setActorUserId(seed.admin.getId());
        em.persist(history);
        if (status == StudentRequest.Status.APPROVED) {
            StudentDocument document = new StudentDocument();
            document.setStudentIndeks(student);
            document.setStudentRequest(request);
            document.setType(DocumentType.CERTIFICATE);
            document.setOriginalName("uverenje-o-polozenim-ispitima.pdf");
            document.setContentType("application/pdf");
            document.setSizeBytes(24576);
            document.setStorageKey("demo/documents/" + key + "-" + type.name() + ".pdf");
            em.persist(document);
            GeneratedCertificate certificate = new GeneratedCertificate();
            certificate.setStudentRequest(request);
            certificate.setStorageKey("demo/certificates/" + key + ".pdf");
            certificate.setVerificationCode("DEMO-CERT-" + key.toUpperCase(Locale.ROOT));
            em.persist(certificate);
        }
        return request;
    }

    private void enrollmentApplication(Seed seed, String key, EnrollmentApplication.Status status, String first,
                                       String last, String email) {
        if (one("select a from EnrollmentApplication a where a.idempotencyKey=:value", "value", key) != null) return;
        EnrollmentApplication application = new EnrollmentApplication();
        application.setIdempotencyKey(key);
        application.setStatus(status);
        application.setIme(first);
        application.setPrezime(last);
        application.setJmbg("DEMO-CANDIDATE-" + key);
        application.setEmail(email);
        application.setUsername(email);
        application.setStudijskiProgramId(seed.program.getId());
        application.setGodina(2026);
        application.setTuitionEur(new BigDecimal("3000.00"));
        if (status == EnrollmentApplication.Status.REJECTED) {
            application.setDecisionReason("Nedostaje dokumentacija.");
            application.setDecidedByUserId(seed.admin.getId());
            application.setDecidedAt(LocalDateTime.now().minusDays(1));
        }
        em.persist(application);
        for (String type : Arrays.asList("DIPLOMA", "IDENTITY_PROOF", "PAYMENT")) {
            EnrollmentDocumentChecklist check = new EnrollmentDocumentChecklist();
            check.setApplication(application);
            check.setDocumentType(type);
            check.setVerified(!type.equals("PAYMENT"));
            em.persist(check);
        }
    }

    private void schedule(Seed seed) {
        Room a1 = room("A1", 120, "Glavna zgrada");
        Room lab1 = room("Lab 1", 32, "IT centar");
        StudentGroup first = group("SI-1A", "Softversko inzenjerstvo - prva godina A");
        StudentGroup second = group("SI-2A", "Softversko inzenjerstvo - druga godina A");
        for (Map.Entry<String, StudentIndeks> item : seed.students.entrySet()) {
            membership(item.getValue(), item.getKey().equals("good") ? second : first);
        }
        session("Programiranje 1 - predavanja", a1, first, seed.professors.get(0), 1, 10);
        session("Baze podataka 1 - vezbe", lab1, first, seed.professors.get(1), 2, 12);
        session("Algoritmi - predavanja", a1, second, seed.professors.get(2), 3, 9);
        examRoom(seed.exams.get("active-SI101"), a1, 80);
        examRoom(seed.exams.get("active-SI109"), lab1, 30);
    }

    private Room room(String code, int capacity, String location) {
        Room room = one("select r from Room r where r.code=:value", "value", code);
        if (room == null) {
            room = new Room(); room.setCode(code); room.setCapacity(capacity); room.setLocation(location); em.persist(room);
        }
        return room;
    }

    private StudentGroup group(String code, String name) {
        StudentGroup group = one("select g from StudentGroup g where g.code=:value", "value", code);
        if (group == null) {
            group = new StudentGroup(); group.setCode(code); group.setName(name); em.persist(group);
        }
        return group;
    }

    private void membership(StudentIndeks student, StudentGroup group) {
        if (one("select m from StudentGroupMembership m where m.studentIndeks=:student and m.studentGroup=:group",
                "student", student, "group", group) == null) {
            StudentGroupMembership membership = new StudentGroupMembership();
            membership.setStudentIndeks(student); membership.setStudentGroup(group); em.persist(membership);
        }
    }

    private void session(String title, Room room, StudentGroup group, Nastavnik professor, int days, int hour) {
        if (one("select s from ClassSession s where s.title=:title and s.studentGroup=:group and s.professor=:professor",
                "title", title, "group", group, "professor", professor) != null) return;
        ClassSession session = new ClassSession();
        session.setTitle(title); session.setRoom(room); session.setStudentGroup(group); session.setProfessor(professor);
        session.setStartsAt(LocalDate.now().plusDays(days).atTime(hour, 0));
        session.setEndsAt(LocalDate.now().plusDays(days).atTime(hour + 2, 0));
        em.persist(session);
    }

    private void examRoom(Ispit exam, Room room, int expected) {
        if (one("select a from ExamRoomAssignment a where a.ispit=:exam and a.room=:room", "exam", exam, "room", room) != null) return;
        ExamRoomAssignment assignment = new ExamRoomAssignment();
        assignment.setIspit(exam); assignment.setRoom(room); assignment.setExpectedStudents(expected); em.persist(assignment);
    }

    private void notifications(Seed seed) {
        notice(seed, "freshman", "EXAM_REGISTRATION", "Ispit je prijavljen", "Prijava ispita Programiranje 1 je evidentirana.", false);
        notice(seed, "debt", "FINANCE_CHARGE", "Novo finansijsko zaduzenje", "Dug od 1.500 EUR blokira prijavu ispita.", false);
        notice(seed, "pendingrequest", "YEAR_ENROLLMENT", "Zahtev ceka odobrenje", "Dokumentacija je kompletna i zahtev ceka admina.", false);
        notice(seed, "good", "YEAR_ENROLLMENT_APPROVED", "Upis godine je odobren", "Demo odobren zahtev za upis naredne godine.", true);
        notice(seed, "conditional", "REQUEST_STATUS", "Potrebna dokumentacija", "Dopunite dokumentaciju za uslovni upis.", true);
    }

    private void notice(Seed seed, String key, String type, String title, String message, boolean read) {
        UserAccount recipient = accountRepo.findByLinkedStudentIndeksId(seed.students.get(key).getId()).orElseThrow(IllegalStateException::new);
        Long count = em.createQuery("select count(n) from Notification n where n.recipient=:recipient and n.type=:type", Long.class)
                .setParameter("recipient", recipient).setParameter("type", type).getSingleResult();
        if (count > 0) return;
        Notification notification = new Notification();
        notification.setRecipient(recipient); notification.setType(type); notification.setTitle(title);
        notification.setMessage(message); notification.setReadFlag(read); em.persist(notification);
    }

    private UserAccount account(String username, Role role, StudentIndeks indeks, Nastavnik professor, StudentPodaci personal) {
        UserAccount account = accountRepo.findByUsername(username).orElseGet(UserAccount::new);
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(PASSWORD));
        account.setRole(role);
        account.setEnabled(true);
        account.setMustChangePassword(false);
        account.setLinkedStudentIndeks(indeks);
        account.setLinkedNastavnik(professor);
        account.setLinkedStudentPodaci(personal);
        return accountRepo.save(account);
    }

    @SuppressWarnings("unchecked")
    private <T> T one(String jpql, Object... params) {
        List<T> result = (List<T>) query(jpql, params).setMaxResults(1).getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> list(String jpql, Object... params) {
        return (List<T>) query(jpql, params).getResultList();
    }

    private javax.persistence.Query query(String jpql, Object... params) {
        javax.persistence.Query query = em.createQuery(jpql);
        for (int i = 0; i < params.length; i += 2) query.setParameter((String) params[i], params[i + 1]);
        return query;
    }

    private static final class Seed {
        UserAccount admin;
        StudijskiProgram program;
        SkolskaGodina activeYear;
        SkolskaGodina nextYear;
        Map<String, Predmet> subjects;
        Map<String, RealizacijaPredmeta> realizations;
        List<Nastavnik> professors;
        Map<String, DrziPredmet> assignments;
        Map<String, StudentIndeks> students;
        List<IspitniRok> periods;
        Map<String, Ispit> exams;
    }
}
