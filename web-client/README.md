# Student Service Web Client

React + TypeScript + Vite SPA for the migrated Student Service.

## Commands

```bash
npm ci
npm run lint
npm run dev
npm run build
```

Always use a clean `npm ci` install from `package-lock.json`. Do not include or reuse a copied `node_modules` directory.

## API/auth behavior

- Uses `credentials: 'include'` on every request.
- Fetches CSRF metadata from `/api/auth/csrf` before mutating calls.
- Sends the configured CSRF header.
- Keeps auth state in React memory only.
- Does not store bearer tokens or long-lived credentials in `localStorage`.

## Route map

Public:

- `/login`

Shared authenticated:

- `/dashboard`
- `/profile`
- `/account`
- `/settings` redirects to `/account`

Student:

- `/student/dashboard`
- `/student/profile`
- `/student/subjects`
- `/student/exams`
- `/student/payments`
- `/student/grades`

Professor:

- `/professor/dashboard`
- `/professor/subjects`
- `/professor/subjects/:id/students`
- `/professor/exams`
- `/professor/exams/:id/results`
- `/professor/predispit`

Admin:

- `/admin/dashboard`
- `/admin/students`
- `/admin/students/new`
- `/admin/students/:id`
- `/admin/students/:id/indexes`
- `/admin/professors`
- `/admin/subjects`
- `/admin/programs`
- `/admin/school-years`
- `/admin/exam-periods`
- `/admin/exams`
- `/admin/enrollments`
- `/admin/payments`
- `/admin/reports`
