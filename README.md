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
5. Temporarily uses the faculty email as the initial password and stores only its BCrypt hash.

The student can then log in with:

```text
username: faculty email
password: faculty email
```

The student should immediately change the initial password from the student profile. The authenticated password-change endpoint is `POST /api/auth/password`.

> TODO: Replace the email-based initial password with a generated temporary password and send it to the student's private email.

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

## Backend setup

From `ServerAndDTOs/`:

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL='jdbc:mysql://localhost:3306/studentski_servis?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='student_service'
export DB_PASSWORD='change-me'
export CORS_ALLOWED_ORIGINS='http://localhost:5173'

./mvnw test
./mvnw install -DskipTests
./mvnw -pl server spring-boot:run
```

The server listens on `http://localhost:8080` by default.

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
