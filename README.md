# Student Service Web Migration

This repository contains the migrated student service platform:

- `ServerAndDTOs/` — the existing Java/Spring Boot backend kept as the core REST API and hardened for web use.
- `web-client/` — a new React + TypeScript + Vite browser client replacing the JavaFX/FXML desktop UI.

The JavaFX repository is retained separately in the delivery as a feature map, not as application code for the web UI.

## Architecture

The backend remains the source of truth for all authentication, authorization, ownership checks, business validation, and persistence. The frontend uses role-based routes for UX, but it does not rely on frontend checks for protection.

Authentication uses server-side session cookies and CSRF protection:

1. The browser obtains a CSRF token from `GET /api/auth/csrf`.
2. The browser submits `POST /api/auth/login` with username/password and the CSRF header.
3. The backend stores the Spring Security context in the HTTP session.
4. The browser sends credentials with every API call.
5. The backend returns the current account with `GET /api/auth/me` and `GET /api/me`.

No bearer token or long-lived credential is stored in `localStorage`.

## Study program and annual subject realization model

The study plan and yearly teaching organization are deliberately separated:

```text
StudijskiProgram
    |
ProgramPredmet
    |
RealizacijaPredmeta
    |-- DrziPredmet
    |-- SlusaPredmet
```

- `ProgramPredmet` is the stable curriculum entry. It connects a subject to a study program and defines its study year, semester, and teaching-hour funds.
- `RealizacijaPredmeta` is the annual realization of one `ProgramPredmet` in one `SkolskaGodina`.
- `DrziPredmet` is a professor assignment to a realization. A realization can have multiple assignments with roles such as `NOSILAC`, `PREDAVANJA`, `VEZBE`, or `PRAKTIKUM`.
- `SlusaPredmet` connects a student index to the annual realization, not to an individual professor.

Professors can therefore change every school year without changing the curriculum or historical student records.

### Expected workflow

1. Admin creates a study program.
2. Admin adds subjects to the program through `ProgramPredmet`, including study year and semester.
3. At the beginning of a school year, annual realizations are generated from the program curriculum.
4. Admin assigns one or more professors to each realization.
5. Creating a student index only connects the student to a study program; it does not enroll all years of subjects.
6. When the student enrolls a study year, the backend ensures annual realizations exist and creates `SlusaPredmet` records for all subjects from that exact curriculum year.
7. Renewal may add selected unresolved realizations from previous years.

Relevant admin APIs:

- `POST /api/realizacija/generate?programId={id}&skolskaGodinaId={optionalId}`
- `GET /api/realizacija/all?skolskaGodinaId={optionalId}`
- `POST /api/drzi/create` with `realizacijaPredmetaId`, `nastavnikId`, and `uloga`
- `POST /api/studij/upis` with `indeksId` and `upisujeGodinu`
- `POST /api/studij/sync-subjects?indeksId={id}` to repair or refresh current subject assignments

## Student lifecycle pipeline

The normal production flow for creating and activating a student is:

```text
StudentPodaci
    |
StudentIndeks
    |
UserAccount (STUDENT)
    |
UpisGodine
    |
SlusaPredmet -> RealizacijaPredmeta -> all DrziPredmet assignments
    |
/api/me/student/** portal
```

### 1. Create personal data

An admin creates `StudentPodaci` through `POST /api/student/add`. The faculty email is required and must be unique because it becomes the student's login username.

Creating personal data alone does not create a login account or assign subjects.

### 2. Create an index and login account

An admin creates the first `StudentIndeks` through `POST /api/student/saveindeks`.

In the same transaction, the backend:

1. Connects the index to the selected study program.
2. Deactivates any previously active index for the same student.
3. Creates a `UserAccount` with role `STUDENT`, or links the existing student account to the new active index.
4. Uses the faculty email as the username.
5. Generates a one-time temporary password, stores only its BCrypt hash, and marks the account with `mustChangePassword=true`.

The admin-only provisioning response displays the random temporary password once. After login, the backend allows only `/api/auth/me`, `/api/auth/password`, and `/api/auth/logout` until the password is changed. Other API calls return `403` with code `MUST_CHANGE_PASSWORD`.

Creating another index for the same student does not create another account or reset the changed password. The existing account is only linked to the new active index.

### 3. Enroll a study year

Creating an index does not automatically assign every subject from the program.

An admin enrolls the student into a specific curriculum year through:

```http
POST /api/studij/upis

{
  "indeksId": 123,
  "upisujeGodinu": 1
}
```

The backend then:

1. Requires an active `SkolskaGodina`.
2. Reads all `ProgramPredmet` entries for the student's program and selected study year.
3. Ensures one `RealizacijaPredmeta` exists for every curriculum subject in the active school year.
4. Creates one `SlusaPredmet` per realization.

`SlusaPredmet` links the student to the realization, not to one professor. Therefore, if three professors are assigned to the same realization, the student sees one subject with all three professor assignments and their roles.

### 4. Synchronize subjects after curriculum changes

If subjects or realizations are added after the student was already enrolled, the existing enrollment is not recreated. An admin can safely synchronize it through:

```http
POST /api/studij/sync-subjects?indeksId=123
```

The operation is idempotent: it creates missing `SlusaPredmet` records without duplicating existing ones. The same action is available as **Sync current subjects** on the admin student-detail screen.

Use synchronization when a student has a valid active-year enrollment but the Subjects tab is empty or missing recently added curriculum subjects.

### 5. Student portal data

After login, the frontend uses ownership-safe `/api/me/student/**` endpoints. The logged-in student never supplies an arbitrary index ID.

| Student screen | Main data |
|---|---|
| Dashboard | Identity, active index, status, school year, current subjects, earned ECTS, average grade, exam registrations, balance |
| Profile | Personal data, active study record, index history, lifecycle history, subjects, exams, finances, password change |
| Subjects | Curriculum code/name, ECTS, year/semester, active school year, all assigned professors and roles |
| Exams | Active registrations and previous attempts with subject, date, time, professor, points, grade, and status |
| Grades | Passed and unresolved subjects |
| Payments | Payment history, remaining balance, and exchange-rate source |
| Requests | Student-service requests and their decision status |
| Notifications | Academic, exam, finance, and request notifications |

If the student account is not linked to an index, `/api/me/student/**` requests are rejected. If the student has no active-year enrollment, current subjects remain empty until the admin enrolls the study year.

## Docker setup

The complete local stack can run through Docker Compose without installing Java, Maven, Node.js, or MySQL on the host. Docker builds the multi-module backend from `ServerAndDTOs/`, builds the Vite frontend with `npm ci`, and serves the frontend through nginx.

For a quick local start using development-only default credentials:

```bash
docker compose up --build
```

To customize credentials or ports, copy `.env.example` to `.env`, replace the example passwords, and then start the stack:

```bash
cp .env.example .env
docker compose up --build
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
docker compose up --build
```

Run in the background with `docker compose up --build -d`. Stop and remove containers and the network with:

```bash
docker compose down
```

To also permanently delete the local MySQL data volume:

```bash
docker compose down -v
```

After startup:

- frontend: `http://localhost:5173`
- backend: `http://localhost:8080`
- MySQL: `localhost:3306`

The production frontend image serves the React SPA and proxies browser requests under `/api` to `backend:8080`. This keeps frontend API calls, session cookies, and CSRF requests on the same browser origin. Direct backend access on port `8080` remains available for API diagnostics.

### Docker environment variables

| Variable | Purpose | Local default |
|---|---|---|
| `MYSQL_DATABASE` | MySQL database created on first startup | `studentski_servis` |
| `MYSQL_USER` | MySQL application user and backend DB username | `student_service` |
| `MYSQL_PASSWORD` | Password for the MySQL application user | development-only Compose default |
| `MYSQL_ROOT_PASSWORD` | MySQL root password used to initialize and health-check MySQL | development-only Compose default |
| `MYSQL_PORT` | MySQL host port | `3306` |
| `BACKEND_PORT` | Backend host port | `8080` |
| `FRONTEND_PORT` | Frontend host port | `5173` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `DEMO_DATA_ENABLED` | Enables the DEV-only full workflow demo seed | `false` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins allowed to call the backend directly | `http://localhost:5173` |
| `BOOTSTRAP_ADMIN_USERNAME` | Username used only when creating the first admin account | `admin@example.local` |
| `BOOTSTRAP_ADMIN_PASSWORD` | Password used only when creating the first admin account | development-only Compose default |

Compose sets the backend `DB_URL` to `jdbc:mysql://mysql:3306/<MYSQL_DATABASE>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`, where `mysql` is the Compose service name. The `dev` profile is intentional for local Docker because it uses Hibernate schema updates and does not require a pre-migrated production database. Use the existing `prod` profile only with a schema prepared by the Flyway migrations and HTTPS-compatible secure-cookie settings.

Do not use the Compose default passwords outside an isolated local machine. The first admin is created only if no admin exists; remove `BOOTSTRAP_ADMIN_USERNAME` and `BOOTSTRAP_ADMIN_PASSWORD` from `.env` after successful bootstrap when using a persistent environment.

### Docker troubleshooting

**Port `3306`, `8080`, or `5173` is already in use**

Change the corresponding `MYSQL_PORT`, `BACKEND_PORT`, or `FRONTEND_PORT` in `.env`, then run `docker compose up --build` again. If `FRONTEND_PORT` changes, also set `CORS_ALLOWED_ORIGINS` to the new frontend URL. The frontend's nginx `/api` proxy continues to work regardless of the chosen host ports.

**Backend cannot connect to MySQL**

Run `docker compose ps` and confirm that `mysql` is healthy, then inspect `docker compose logs mysql backend`. The backend must use `mysql:3306`, not `localhost:3306`, inside Compose. If database credentials were changed after the volume was initialized, recreate the local database with `docker compose down -v` and start again; this deletes existing local data.

**Frontend cannot reach the backend**

Inspect `docker compose logs frontend backend` and open `http://localhost:5173/api/auth/csrf`. A successful response confirms the nginx-to-backend route. Browser requests should use relative `/api/...` paths; `http://backend:8080` is only resolvable inside the Compose network.

**CORS or session-cookie problem**

Use the frontend URL consistently instead of mixing `localhost` and `127.0.0.1`. For direct calls to port `8080`, ensure the exact browser origin is listed in `CORS_ALLOWED_ORIGINS` and that requests include credentials. Local Docker uses the `dev` profile and non-secure cookies over HTTP; the `prod` profile enables secure session cookies and therefore requires HTTPS.

## Backend setup

From `ServerAndDTOs/`:

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL='jdbc:mysql://localhost:3306/studentski_servis?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='student_service'
export DB_PASSWORD='change-me'
export CORS_ALLOWED_ORIGINS='http://localhost:5173'
export BOOTSTRAP_ADMIN_USERNAME='admin@example.edu'
export BOOTSTRAP_ADMIN_PASSWORD='replace-with-a-strong-secret'

./mvnw test
./mvnw install -DskipTests
./mvnw -pl server spring-boot:run
```

The server listens on `http://localhost:8080` by default.

### First admin bootstrap

When no `ADMIN` account exists, set `BOOTSTRAP_ADMIN_USERNAME` and `BOOTSTRAP_ADMIN_PASSWORD` before one server start. The initializer creates exactly one enabled admin account and becomes a no-op as soon as any admin exists. Remove the bootstrap credentials from the environment after the first successful start.

On Windows PowerShell, install the current multi-module artifacts before starting the
server so the runtime does not pick up an older DTO snapshot:

```powershell
.\mvnw.cmd test
.\mvnw.cmd install -DskipTests
.\mvnw.cmd -pl server spring-boot:run
```

### Production backend configuration

Use `SPRING_PROFILES_ACTIVE=prod` and provide secrets only through the environment:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_URL='jdbc:mysql://db-host:3306/studentski_servis?useSSL=true&serverTimezone=UTC'
export DB_USERNAME='student_service'
export DB_PASSWORD='...'
export CORS_ALLOWED_ORIGINS='https://student-service.example.edu'
export COOKIE_SAME_SITE='Lax'
```

Production uses:

- `spring.jpa.hibernate.ddl-auto=validate`
- Flyway migrations from `server/src/main/resources/db/migration`
- secure session cookies when `server.servlet.session.cookie.secure=true`
- centralized CORS configuration in `SecurityConfig`
- a cookie filter that appends SameSite and Secure attributes to `Set-Cookie` headers where needed

Do not use `ddl-auto=update` as the production schema strategy.

## Frontend setup

From `web-client/`, install from the lockfile instead of reusing a copied or ZIP-provided `node_modules` directory:

```bash
npm ci
npm run lint
npm run dev
```

The Vite dev server runs on `http://localhost:5173` and proxies `/api` to `http://localhost:8080`.

For production builds:

```bash
npm ci
npm run lint
npm run build
```

Set `VITE_API_BASE_URL` if the API is not served from the same origin.

## Full study lifecycle test pipeline

The repository includes an isolated Spring/H2 lifecycle integration pipeline and a Playwright role-route smoke suite. It creates an `E2E_TEST_<UUID>` four-year program, simulates exam attempts and progression, verifies regular/conditional/renewal enrollment, checks negative rules and duplicate prevention, and contracts critical backend flows to frontend routes/API helpers.

From `ServerAndDTOs/`:

```powershell
$env:JAVA_HOME='C:\Users\gamek\.jdks\ms-11.0.30'
.\mvnw.cmd test
```

From `web-client/`:

```powershell
npm ci
npx playwright install chromium
npm run lint
npm run e2e
npm run e2e:live
```

`npm run e2e:live` starts an isolated Spring/H2 backend and verifies real ADMIN, STUDENT, and PROFESSOR browser login and role routes. Detailed coverage, frontend availability map, fixes, and limitations: [`docs/E2E_STUDY_PIPELINE_TESTS.md`](docs/E2E_STUDY_PIPELINE_TESTS.md).

Detailed step-by-step walkthrough of the four-year Spring lifecycle test: [`docs/FULL_FOUR_YEAR_TEST_WALKTHROUGH.md`](docs/FULL_FOUR_YEAR_TEST_WALKTHROUGH.md).

## Roles

The only user-facing roles are:

- `STUDENT`
- `PROFESSOR`
- `ADMIN`

The code uses an enum and centralized authorization helper so permissions can be extended later without introducing extra visible roles.

## Auth endpoints

| Method | Endpoint | Access | Purpose |
|---|---|---:|---|
| GET | `/api/auth/csrf` | Public | Return CSRF token/header metadata for SPA forms. |
| POST | `/api/auth/login` | Public + CSRF | Authenticate username/password, create server session. |
| POST | `/api/auth/logout` | Authenticated + CSRF | Invalidate server session. |
| GET | `/api/auth/me` | Authenticated | Return authenticated account DTO. |
| POST | `/api/auth/password` | Authenticated + CSRF | Change the current account password after verifying the current password. |
| GET | `/api/me` | Authenticated | Return authenticated account DTO through the safer `me` namespace. |

## Safer `me` endpoints

Student UI screens call these endpoints rather than arbitrary `studentIndeksId` URLs:

- `GET /api/me/student/dashboard`
- `GET /api/me/student/profile`
- `GET /api/me/student/subjects`
- `GET /api/me/student/exams`
- `GET /api/me/student/payments`

Professor UI screens call:

- `GET /api/me/professor/dashboard`
- `GET /api/me/professor/subjects`
- `GET /api/me/professor/exams`

Professor records do not automatically have login access. Admin provisions the linked account through:

- `POST /api/nastavnik/{id}/provision-account`

The response contains the one-time temporary password only when the account is first created. A second call returns the existing linked account without resetting its password.

## End-to-end pipelines

**Admin:** bootstrap/login -> activate school year -> create program and subjects -> generate annual realizations -> create professor and provision account -> assign professor -> create student/index account -> enroll study year -> create exam period/exam -> process finances and requests.

**Student:** login with temporary password -> mandatory password change -> view index/subjects -> register or withdraw exam -> view attempt history and locked grades/ECTS -> view ledger-backed balance -> submit request -> download approved certificate.

**Professor:** login with temporary password -> mandatory password change -> view assigned subjects/exams only -> view registered students -> enter results while unlocked -> lock results -> student statuses and ECTS are recalculated.

## Manual test checklist

- ADMIN: first login, active school year, program, subject, realization, professor account, assignment, student/index account, study-year enrollment, exam period/exam, ledger payment/obligation/reversal, request approval.
- STUDENT: temporary-password redirect, password change, profile/index, subjects, exam registration/withdrawal/history, grades/ECTS, finance balance, request, approved document download, notifications.
- PROFESSOR: temporary-password redirect, password change, own subjects only, own exam registrations, result entry, result lock, notifications.

## Role and permission matrix

| Area | STUDENT | PROFESSOR | ADMIN |
|---|---|---|---|
| Auth login/logout/me | Own session | Own session | Own session |
| Student profile/dashboard | Own linked student/index only | No general access | All |
| Student payments/saldo | Own linked index only | No general access | All |
| Student add/index assignment | No | No | Yes |
| Student search/admin lists | No | Limited only where explicitly implemented | Yes |
| Teaching assignments (`DrziPredmet`) | No | Read own assigned subjects/students | Create/update/delete/read all |
| Exam results | Own result through `me` routes only | Own assigned exams only | All |
| Exam result updates/attendance/cancel | No | Own assigned, only when exam is unlocked | All, including explicit override where policy allows |
| Predispit obligations | Own read where exposed | Own assigned subjects only | All |
| Study programs / subjects catalog | Authenticated read | Authenticated read | Write/admin screens |
| School years, programs, enrollments | No | No | Yes |
| Reports | No | Assigned data only where later exposed | Yes |

## Migration notes from desktop to web

The JavaFX/FXML client was not ported directly. Its controllers were used as a feature map:

| Desktop controller | Web replacement |
|---|---|
| `StudentsAdminController` | `/admin/students`, `/admin/students/new`, `/admin/students/:id`, `/admin/students/:id/indexes` |
| `StudentsSearchController` | `/admin/students` search and pagination |
| `StudentsByIndexController` | Admin search/detail flow through secured student endpoints |
| `StudentsBySrednjaController` | Admin student search extension point |
| `StudentProfileController` | `/student/profile`, `/student/dashboard`, admin student detail |
| `NastavniciController` | `/admin/professors`, professor assignment-backed screens |
| `ProgramsController` | `/admin/programs`, `/admin/subjects` |
| `ExamsController` | `/admin/exams`, `/professor/exams`, `/professor/exams/:id/results` |
| `ReportsController` | `/admin/reports`; JasperReports should remain backend-generated PDFs if still required |

## Backend changes implemented

- Removed the blanket `permitAll()` security posture and added authenticated route protection.
- Added `UserAccount`, `Role`, BCrypt password support, and session-based auth endpoints.
- Added centralized CORS and cookie/CSRF handling for SPA authentication.
- Added `CurrentUser` helper with student/professor/admin ownership checks.
- Added safer `/api/me/**` endpoints for student and professor screens.
- Added the missing study program API at `/api/studprogram/all/sorted` and related selection endpoints.
- Expanded student dashboard data aggregation into `StudentDashboardDTO`.
- Fixed the exam average query so `/api/ispit/{ispitId}/prosek` filters by exam id, not subject id.
- Restored exam registration validation and added locked-exam blocking rules.
- Centralized grading/points logic and audited manual grade overrides.
- Added duplicate checks for subject recognition.
- Refactored payments to `BigDecimal`, stored exchange rate per payment, explicit fallback visibility, and validated positive amounts.
- Made index assignment transaction-safe using a pessimistic allocation lock and retry on unique conflicts.
- Enforced one active school year at the service layer and added a DB-level generated-column unique backstop in the migration.
- Replaced exposed JPA entities with DTO responses in the targeted controllers.
- Added a global sanitized API error format.
- Moved datasource credentials to environment variables.
- Added Flyway migrations and test/dev/prod profiles.
- Added tests for auth, ownership checks, exam registration validation, locked exams, grading, payments, index-number gap allocation, and the average-query bug.

## API error format

All expected errors use the same shape:

```json
{
  "timestamp": "2026-06-08T12:00:00Z",
  "status": 403,
  "error": "Forbidden",
  "code": "FORBIDDEN",
  "message": "Student može pristupiti samo sopstvenom indeksu.",
  "path": "/api/student/profile/123"
}
```

HTTP status policy:

- `400` validation or business input error
- `401` unauthenticated
- `403` forbidden
- `404` missing resource
- `409` duplicate/state conflict
- `500` sanitized unexpected server error

## Business-rule assumptions still to confirm

These are implemented conservatively and should be confirmed with the faculty before production rollout:

1. Professor updates are blocked on locked exams unless an admin override is used.
2. The default tuition is configured globally via `TUITION_DEFAULT_EUR`; a full tuition table by school year, program, and financing type is left as an extension point.
3. Exam registration deadlines are derived from exam/exam-period dates when no dedicated registration-deadline table exists.
4. Manual grade override is allowed only through backend paths that already passed authorization and is audited.
5. JasperReports are not migrated to the frontend; report generation should remain a backend PDF/download responsibility.

## Tok upisa/obnove godine

Tok upisa postojeceg studenta odvojen je od postojeceg `EnrollmentApplication` toka, koji sluzi za prijem potpuno novog studenta i kreiranje naloga/indeksa.

`StudentIndeks.godina` ostaje godina upisa koja je deo broja indeksa. Trenutna godina studija i skolska godina izvode se iz najnovijeg `UpisGodine` zapisa, kako se ne bi duplirali i razisli podaci.

### Studentski tok

1. Student otvara `/student/year-enrollment`.
2. Backend iz polozenih/priznatih ispita racuna ostvarene ESPB bodove i prikazuje polozene i nepolozene predmete.
3. Sistem nalazi najnoviji upis godine i sledecu vec konfigurisanu `SkolskaGodina`.
4. Sistem predlaze `ENROLL_NEXT_YEAR`, `CONDITIONAL_ENROLLMENT` ili `RENEW_YEAR`.
5. Student bira nepolozene predmete za prenos i podnosi zahtev.
6. Zahtev prelazi u `PENDING_DOCUMENTS`. Student mora uzivo da potpise ugovor i donese potvrdu uplate i potrebnu dokumentaciju.
7. Student prati status, moze da otkaze nezavrsen zahtev i dopuni zahtev koji je vracen u `NEEDS_CHANGES`.

Podrazumevani pragovi su kumulativni:

- redovan upis: `48 * trenutnaGodinaStudija` ESPB;
- uslovni upis: `37 * trenutnaGodinaStudija` ESPB;
- ispod uslovnog praga predlaze se obnova iste godine;
- `ECTSRule` za program/ciljnu godinu ima prednost nad podrazumevanim pragom redovnog upisa;
- maksimalan zbir izabranih prenetih predmeta je 60 ESPB.

Pragovi su izdvojeni u `StudyYearEnrollmentPolicy` i podesivi promenljivama:

- `ENROLLMENT_REGULAR_MINIMUM_ECTS_PER_YEAR`
- `ENROLLMENT_CONDITIONAL_MINIMUM_ECTS_PER_YEAR`
- `ENROLLMENT_TRANSFER_MAX_ECTS`

Polozeni predmeti se proveravaju na backendu i ne mogu biti preneti. Za obnovu mora biti izabran najmanje jedan nepolozeni predmet ako takvi predmeti postoje.

### Admin tok

Admin sa `ENROLLMENT_WRITE` dozvolom otvara `/admin/year-enrollments`, filtrira zahteve, otvara detalje i proverava:

- studenta, indeks, trenutnu i trazenu godinu studija;
- trenutnu i ciljnu skolsku godinu;
- ESPB snapshot iz trenutka podnosenja;
- izabrane prenete predmete;
- ugovor, uplatu i ostalu dokumentaciju;
- istoriju statusa i napomene.

Kada su ugovor, uplata i dokumentacija potvrdjeni, zahtev prelazi u `PENDING_ADMIN_APPROVAL`. Admin zatim moze da ga odobri, odbije sa razlogom ili vrati na dopunu.

Samo admin approval, u jednoj transakciji, stvarno menja evidenciju:

1. ponovo proverava ESPB i da izabrani predmeti nisu polozjeni;
2. kreira `UpisGodine` za ciljnu skolsku godinu;
3. za obnovu dodatno kreira povezani `ObnovaGodine`;
4. za redovan/uslovni upis dodaje predmete nove godine i izabrane zaostatke;
5. za obnovu dodaje izabrane nepolozene predmete;
6. kreira/koristi `RealizacijaPredmeta` za ciljnu skolsku godinu i upisuje `SlusaPredmet`;
7. osvezava cached `ostvarenoEspb`, belezi admina, status istoriju/audit i salje notifikaciju studentu.

Student ne moze direktno da pozove admin approval niti `/api/studij/**` admin rute.

### API endpointi

| Method | Endpoint | Uloga | Namena |
|---|---|---|---|
| GET | `/api/enrollment/year-requests/me/eligibility` | STUDENT | Trenutno stanje, ESPB, predlog i predmeti za prenos |
| GET | `/api/enrollment/year-requests/me` | STUDENT | Istorija sopstvenih zahteva |
| POST | `/api/enrollment/year-requests/me` | STUDENT | Podnosenje zahteva |
| PUT | `/api/enrollment/year-requests/me/{id}` | STUDENT | Dopuna i ponovno slanje |
| POST | `/api/enrollment/year-requests/me/{id}/cancel` | STUDENT | Otkazivanje nezavrsenog zahteva |
| GET | `/api/enrollment/year-requests/admin` | ADMIN + `ENROLLMENT_WRITE` | Lista i filteri po statusu/tipu/skolskoj godini/indeksu |
| GET | `/api/enrollment/year-requests/admin/{id}` | ADMIN + `ENROLLMENT_WRITE` | Detalji zahteva |
| PATCH | `/api/enrollment/year-requests/admin/{id}/checklist` | ADMIN + `ENROLLMENT_WRITE` | Potvrda ugovora/uplate/dokumentacije |
| POST | `/api/enrollment/year-requests/admin/{id}/approve` | ADMIN + `ENROLLMENT_WRITE` | Transakciono odobrenje i stvarni upis |
| POST | `/api/enrollment/year-requests/admin/{id}/reject` | ADMIN + `ENROLLMENT_WRITE` | Odbijanje sa razlogom |
| POST | `/api/enrollment/year-requests/admin/{id}/needs-changes` | ADMIN + `ENROLLMENT_WRITE` | Vracanje na dopunu |

Produkcijska sema se prosiruje migracijom `V11__study_year_enrollment_requests.sql`.

### Trenutna ogranicenja i sledeca unapredjenja

- Sledeca skolska godina mora unapred postojati u `SkolskaGodina`; sistem namerno ne kreira godinu automatski.
- Naziv skolske godine treba da pocinje godinom, na primer `2026/2027` ili `26/27`, da bi sledeca godina mogla pouzdano da se odredi.
- Ugovor, uplata i dokumentacija su trenutno admin-verifikovani flagovi; upload, verzionisanje i digitalni potpis nisu deo ovog toka.
- Conditional enrollment trenutno nema poseban finansijski plan ili dodatni ugovor.
- ESPB se racuna iz polozenih/priznatih `PrijavaIspita`; `StudentIndeks.ostvarenoEspb` je cached vrednost koja se osvezava pri zakljucavanju rezultata i admin approval-u.
- Migraciju treba proveriti na tacnoj produkcijskoj MySQL verziji i realnom backup-u pre pustanja.
- Sledeci koraci su konfiguracija pragova kroz admin UI, upload dokumenata, povezivanje payment flaga sa ledger pravilima i detaljniji izvestaji/audit dashboard.

## Demo seed data

Kompletan demo dataset postoji samo za Spring `dev` profil i podrazumevano je iskljucen. Ukljucuje se promenljivom:

```bash
DEMO_DATA_ENABLED=true
```

ili direktnim Spring property-jem:

```bash
--spring.profiles.active=dev --app.seed.demo-data.enabled=true
```

Initializer `DevDemoDataInitializer` ne postoji u `prod` ni `test` profilu bez eksplicitnog ukljucivanja. Radi u jednoj transakciji, koristi jedinstvena poslovna polja i cuva verzionisani marker, pa drugi start ne pravi duplikate. Dataset koristi aktivnu skolsku godinu `2025/26`, ciljnu `2026/27`, program `SI` sa 40 predmeta i zajednicku lozinku `DemoPass123!`.

| Uloga / scenario | Username | Opis |
|---|---|---|
| Admin | `admin@demo.edu` | Svi admin i permission-gated ekrani |
| Profesor | `marko.aleksic@demo.edu` | Predmeti, studenti, ispiti i predispitne obaveze |
| Profesor | `jelena.jovanovic@demo.edu` | Predmeti, studenti, ispiti i predispitne obaveze |
| Profesor | `nikola.petrovic@demo.edu` | Predmeti, studenti, ispiti i predispitne obaveze |
| Brucos | `student.freshman@demo.edu` | Aktivni predmeti, otvorena prijava ispita i mali saldo |
| Dobar student | `student.good@demo.edu` | Ocene 8/9/10, 96 ESPB, istorija i redovan upis |
| Uslovni upis | `student.conditional@demo.edu` | 42 ESPB, nepolozeni predmeti i `CONDITIONAL_ENROLLMENT` |
| Obnova godine | `student.renewal@demo.edu` | 18 ESPB, vise prenetih predmeta i `RENEW_YEAR` |
| Student sa dugom | `student.debt@demo.edu` | Dug od 1.500 EUR blokira prijavu ispita |
| Zahtev na cekanju | `student.pendingrequest@demo.edu` | Kompletan zahtev koji ceka admin approval |

Pokriveni su realizacije i angazovanja profesora, slusanje predmeta, otvoreni/prosli/buduci rokovi, polozene/pale/odsutne/odjavljene prijave, gradebook sa ukupno 30 predispitnih poena, konzistentan ledger (placeno, dug, delimicna uplata i preplata), zahtevi za upis godine u vise statusa, dokumenti i potvrde, prijave za upis, sale, grupe, raspored i procitane/neprocitane notifikacije.

> Demo seed nije namenjen produkciji. Nemojte ukljucivati `app.seed.demo-data.enabled` uz `prod` profil.

## Verification

Run the backend tests from `ServerAndDTOs/`:

```bash
./mvnw test
```

Run the frontend typecheck/build from `web-client/`:

```bash
npm ci
npm run lint
npm run build
```
