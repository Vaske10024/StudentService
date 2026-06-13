# API and Authentication Documentation

## Authentication model

The migrated web app uses Spring Security with server-side sessions, BCrypt password hashes, CSRF protection, and centralized CORS. The frontend sends cookies with `credentials: 'include'`; no long-lived bearer token is stored in browser storage.

## Public endpoints

| Method | Endpoint | Notes |
|---|---|---|
| GET | `/api/auth/csrf` | Returns CSRF token, header name, and parameter name. |
| POST | `/api/auth/login` | Public authentication endpoint; still requires CSRF header. |

## Authenticated endpoints

| Method | Endpoint | Notes |
|---|---|---|
| POST | `/api/auth/logout` | Invalidates the HTTP session. |
| GET | `/api/auth/me` | Returns the current account. |
| GET | `/api/me` | Same account DTO through the safer user namespace. |

## Request sequence

```text
GET  /api/auth/csrf
POST /api/auth/login   X-XSRF-TOKEN: <token>
GET  /api/auth/me      Cookie: STUDENT_SERVICE_SESSION=...
POST /api/auth/logout  X-XSRF-TOKEN: <token>
```

## Account DTO

```json
{
  "user": {
    "id": 1,
    "username": "admin@example.edu",
    "role": "ADMIN",
    "enabled": true,
    "linkedStudentPodaciId": null,
    "linkedStudentIndeksId": null,
    "linkedNastavnikId": null
  }
}
```

## Safer `me` endpoints

Student endpoints derive the student identity from the logged-in account:

- `GET /api/me/student/dashboard`
- `GET /api/me/student/profile`
- `GET /api/me/student/subjects`
- `GET /api/me/student/exams`
- `GET /api/me/student/payments`

Professor endpoints derive the professor identity from the logged-in account:

- `GET /api/me/professor/dashboard`
- `GET /api/me/professor/subjects`
- `GET /api/me/professor/exams`

## Error response

```json
{
  "timestamp": "2026-06-08T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "code": "STATE_CONFLICT",
  "message": "Ispit je zaključan – rezultat se ne može menjati.",
  "path": "/api/ispit/rezultat"
}
```
