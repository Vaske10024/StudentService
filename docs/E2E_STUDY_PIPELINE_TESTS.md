# Full Study Lifecycle Test Pipeline

## Purpose

The pipeline verifies a real multi-year student-service workflow against the existing Spring services, JPA repositories, H2 test profile, security accounts, and React routes. Test data is isolated with an `E2E_TEST_<UUID>` prefix and every backend scenario runs in a rolled-back transaction.

The standard Maven test command runs both the existing unit/repository tests and `FullStudyLifecycleIT`.

## Backend Scenario

`FullStudyLifecycleIT` and `E2ETestDataFactory` create:

- one four-year/eight-semester program named `E2E_TEST - Digitalno inzenjerstvo i AI`;
- five sequential school years;
- sixteen subjects, four per study year, each worth 15 ECTS;
- annual subject realizations and professor assignments;
- exam periods, exams, pre-exam definitions, and scores;
- an admin, a professor account, a main student, a conditional-enrollment student, and a renewal student.

The main student scenario verifies:

1. Student account provisioning and mandatory temporary-password change.
2. First-year enrollment and visibility of exactly the enrolled subjects.
3. Rejection of an exam for a subject the student does not attend.
4. Valid registrations, pre-exam points, exam attendance, total points, grades, locking, ECTS, and average grade.
5. Student cancellation with reason and preserved history.
6. First-year completion and `ENROLL_NEXT_YEAR` eligibility.
7. Duplicate active request rejection.
8. Admin request list, incomplete checklist rejection, completed checklist, and approval.
9. Second-year subject assignment.
10. Failed exam, later retake and pass, both attempts in history, and rejection after the subject is passed.
11. Administrative voiding with reason and no incorrect ECTS impact.
12. Idempotent `sync-subjects`.
13. Professor ownership denial for another professor's exam.
14. Completion of all remaining second-year subjects and regular enrollment into years three and four.
15. Completion of every third- and fourth-year subject, ending with 240 ECTS, sixteen passed subjects, and four enrollment-history records.

Additional scenarios verify:

- `CONDITIONAL_ENROLLMENT` at 45 ECTS, with new and transferred subjects;
- `RENEW_YEAR` below the conditional threshold, renewal history, selected unresolved subjects, and no duplicate `SlusaPredmet` records;
- an absent student with a manually high grade does not earn ECTS or appear as passed;
- an index without an active-year enrollment receives no current subjects.

## Bugs Found And Fixed

| Problem | Fix | Covered by |
|---|---|---|
| A grade of 6+ could count as passed even when the student did not attend | Passed/average/ECTS queries now require attendance or a recognized subject | `absentStudentWithHighManualGradeDoesNotEarnEctsOrAppearPassed` |
| Student-cancelled registrations disappeared from previous-attempt history | `ODJAVLJEN` is included in previous attempts | main lifecycle test |
| Duplicate year request returned generic `YEAR_ENROLLMENT_NOT_AVAILABLE` | Duplicate check now returns `DUPLICATE_ACTIVE_YEAR_REQUEST` before eligibility short-circuit | main lifecycle test |
| Activating a school year could merge a detached entity and trigger orphan-collection failure | Reload the managed target after bulk deactivation | conditional/renewal scenario |
| Multiple Spring test contexts shared one named H2 database and emitted duplicate-constraint warnings | Each test context now receives an isolated random H2 database | full Maven suite |

## Frontend Availability Map

`FrontendAvailabilityReportTest` is a source-level contract that fails Maven tests if a critical route or API helper disappears. Playwright additionally renders the key routes for all roles with deterministic API fixtures.

| Functionality | Backend endpoint | Frontend status | Recommendation | Minimal support added |
|---|---|---|---|---|
| Create student | `POST /api/student/add` | available | Keep validation aligned with DTO | no |
| Create index/account | `POST /api/student/saveindeks/provision` | available | Keep one-time password visible only once | no |
| Enroll study year | `POST /api/studij/upis` | available | Use year-request approval for normal later-year flow | no |
| Sync subjects | `POST /api/studij/sync-subjects` | available | Keep action idempotent | no |
| Student detail | `GET /api/student/podaci/{id}`, dashboard APIs | available | Add richer audit history later | no |
| Create professor/account | `POST /api/nastavnik/add`, provision endpoint | available | Keep ownership tests | no |
| Create study program | `POST /api/studprogram` | available | Keep program validation aligned with study-type CRUD | yes |
| Create/activate school year | `POST /api/sg`, `PATCH /api/sg/{id}/aktiviraj` | available | Consider confirmation before activation | yes |
| Create/link subject | `POST /api/predmet/admin/create` | available | Existing form creates and links in one step | no |
| Generate realization / assign professor | `/api/realizacija/generate`, `/api/drzi/create` | available | Add bulk assignment later | no |
| Create exam period / exam | `/api/rok/create`, `/api/ispit/admin/create` | available | Add bulk exam generation later | no |
| Student dashboard/subjects/exams | `/api/me/student/**` | available | Keep ownership-safe endpoints | no |
| Student exam cancellation | `PATCH /api/ispit/prijava/{id}/odjavi` | available | Preserve cancellation history | no |
| Admin/professor void registration | `PATCH /api/ispit/prijava/{id}/ponisti` | available | Keep reason mandatory and preserve history | yes |
| Student year request | `/api/enrollment/year-requests/me/**` | available | Keep eligibility message visible | no |
| Admin checklist/approval | `/api/enrollment/year-requests/admin/**` | available | Keep incomplete-checklist approval disabled | no |
| Professor subjects/exams/students/results | `/api/me/professor/**`, `/api/ispit/**` | available | Add explicit ownership-denied UI feedback | no |
| Pre-exam obligations and points | `/api/predispit/admin/**` | available | Keep total maximum visible | no |
| Recognize transferred subject | `POST /api/ispit/priznaj` | available | Keep grade and source note auditable | yes |
| Study-type administration | `/api/studprogram/vrste/**` | available | Prevent deletion while referenced by a program | yes |

## Frontend Smoke Test

`web-client/tests/e2e/role-routes.spec.ts` uses Playwright and deterministic network fixtures. It verifies that key ADMIN, STUDENT, and PROFESSOR routes render an `h1` and produce no browser runtime errors.

The fast smoke test deliberately does not require a running backend or shared database. The Spring integration test covers real backend state transitions; the fast Playwright test covers route/component availability.

`web-client/tests/live/full-stack-role-routes.spec.ts` is the live full-stack suite. It starts Spring with the isolated `e2e` H2 profile, starts Vite on port 5174, performs real CSRF/session login for ADMIN, STUDENT, and PROFESSOR, renders role routes against the real backend, and creates a study type through the admin UI.

## Commands

Backend, from `ServerAndDTOs`:

```powershell
$env:JAVA_HOME='C:\Users\gamek\.jdks\ms-11.0.30'
.\mvnw.cmd test
```

Frontend, from `web-client`:

```powershell
npm ci
npx playwright install chromium
npm run lint
npm run e2e
npm run e2e:live
```

## Known Limitations And Future Work

- The fast Playwright suite uses deterministic API fixtures; `npm run e2e:live` separately verifies real browser login and frontend-to-Spring communication.
- The scenario completes all four years, but the detailed fail/retake/cancellation branches are intentionally concentrated in years one and two.
- Finance, documents, notifications, graduation, scheduling, and program transfer have routes/services but are outside this study-progression pipeline.
- The live suite verifies representative role routes and a real admin mutation, while the deeper four-year lifecycle remains a Spring integration test rather than a browser-driven workflow.
