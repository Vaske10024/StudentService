package org.raflab.studsluzba.e2e;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendAvailabilityReportTest {
    private final Path frontend = Paths.get(System.getProperty("user.dir"))
            .resolve("../../web-client").normalize();

    @Test
    void criticalBackendFlowsHaveFrontendRoutesAndApiWiring() throws IOException {
        String app = read("src/App.tsx");
        String adminApi = read("src/api/admin.ts");
        String adminPages = read("src/pages/AdminPages.tsx") + read("src/pages/AdminCatalogPages.tsx");
        String studentPages = read("src/pages/StudentPages.tsx") + read("src/pages/YearEnrollmentPages.tsx");
        String professorPages = read("src/pages/ProfessorPages.tsx");
        String professorApi = read("src/api/professor.ts");
        String yearApi = read("src/api/yearEnrollment.ts");

        assertContains(app,
                "/admin/students/new", "/admin/students/:id", "/admin/professors", "/admin/subjects",
                "/admin/programs", "/admin/school-years", "/admin/exam-periods", "/admin/exams",
                "/admin/exams/:id/results",
                "/student/dashboard", "/student/subjects", "/student/exams", "/student/year-enrollment",
                "/admin/year-enrollments", "/professor/subjects", "/professor/exams",
                "/professor/exams/:id/registered", "/professor/exams/:id/results", "/professor/predispit");

        assertContains(adminApi,
                "/api/student/saveindeks/provision", "/api/studij/upis",
                "/api/studij/sync-subjects", "/api/nastavnik/add", "/api/predmet/admin/create",
                "/api/studprogram", "/api/sg", "/api/realizacija/generate", "/api/drzi/create",
                "/api/rok/create", "/api/ispit/admin/create", "/api/studprogram/vrste",
                "/api/ispit/priznaj", "/api/ispit/prijava/");
        assertContains(adminPages,
                "/api/student/add", "Create index", "Enroll study year", "Sync current subjects", "Create professor",
                "Create program", "Create school year", "Generate realizations", "Assign professor",
                "Recognize subject", "Study types");
        assertContains(studentPages, "Dostupni ispiti", "Prethodni izlasci", "Podnesi zahtev");
        assertContains(yearApi, "/api/enrollment/year-requests/me/eligibility",
                "/api/enrollment/year-requests/admin", "/checklist", "/approve");
        assertContains(professorPages, "Registered students", "Exam results", "Predispitne obaveze", "Ponisti prijavu");
        assertContains(professorApi, "/api/ispit/prijava/rezultat", "/api/ispit/izlazak",
                "/api/predispit/admin/ostvareno", "/ponisti");
        assertContains(read("playwright.live.config.ts"), "tests/live", "8081");
    }

    private String read(String relative) throws IOException {
        Path file = frontend.resolve(relative);
        assertThat(file).as("frontend file %s", relative).exists();
        return Files.readString(file);
    }

    private void assertContains(String text, String... expected) {
        for (String item : expected) {
            assertThat(text).as("frontend availability marker %s", item).contains(item);
        }
    }
}
