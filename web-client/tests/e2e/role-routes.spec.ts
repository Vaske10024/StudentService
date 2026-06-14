import { expect, Page, test } from '@playwright/test';

type Role = 'ADMIN' | 'STUDENT' | 'PROFESSOR';

const dashboard = {
  student: { ime: 'E2E', prezime: 'Student', emailFakultetski: 'e2e@student.test' },
  activeIndex: { id: 1, broj: 1, godina: 2100, studProgramOznaka: 'E2E', aktivan: true },
  allIndexes: [],
  currentSubjects: [],
  passedSubjects: [],
  failedOrNotPassedSubjects: [],
  activeExamRegistrations: [],
  previousExamAttempts: [],
  studyEnrollments: [],
  renewals: [],
  payments: [],
  balance: { debtEur: 0, creditEur: 0, balanceEur: 0 },
  schoolYear: { id: 1, godina: '2100/2101', aktivna: true },
  status: { status: 'AKTIVAN' },
  statusHistory: []
};

async function mockRole(page: Page, role: Role) {
  const errors: string[] = [];
  page.on('pageerror', (error) => errors.push(error.message));
  const permissions = role === 'ADMIN'
    ? ['ENROLLMENT_WRITE', 'DOCUMENT_DECIDE', 'FINANCE_WRITE', 'REPORT_EXPORT']
    : [];
  await page.route('**/api/**', async (route) => {
    const url = new URL(route.request().url());
    const path = url.pathname;
    if (!path.startsWith('/api/')) {
      await route.continue();
      return;
    }
    let body: unknown = [];
    if (path === '/api/auth/me') {
      body = { user: { id: 1, username: `${role.toLowerCase()}@e2e.test`, role, enabled: true, mustChangePassword: false, permissions, linkedStudentIndeksId: role === 'STUDENT' ? 1 : null, linkedNastavnikId: role === 'PROFESSOR' ? 1 : null } };
    } else if (path === '/api/auth/csrf') {
      body = { token: 'e2e' };
    } else if (path === '/api/me/student/dashboard' || path === '/api/me/student/exams' || path === '/api/me/student/payments') {
      body = dashboard;
    } else if (path === '/api/me/professor/dashboard') {
      body = { user: { role: 'PROFESSOR' }, subjects: [], exams: [] };
    } else if (path === '/api/enrollment/year-requests/me/eligibility') {
      body = { indeksId: 1, earnedEcts: 60, suggestedType: 'ENROLL_NEXT_YEAR', canSubmit: true, message: 'E2E', passedSubjects: [], transferableSubjects: [], currentSchoolYear: { id: 1, godina: '2100/2101' }, targetSchoolYear: { id: 2, godina: '2101/2102' } };
    } else if (path === '/api/student/svi' || path === '/api/student/global-search') {
      body = { content: [], totalElements: 0, totalPages: 0, number: 0, size: 20 };
    } else if (path.startsWith('/api/student/podaci/') || path.startsWith('/api/student/dashboard/')) {
      body = dashboard;
    } else if (route.request().method() !== 'GET') {
      body = {};
    }
    await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify(body) });
  });
  return errors;
}

async function assertRoutes(page: Page, routes: string[]) {
  for (const route of routes) {
    await page.goto(route);
    await expect(page.locator('h1').first()).toBeVisible();
  }
}

test('ADMIN key routes render without runtime errors', async ({ page }) => {
  const errors = await mockRole(page, 'ADMIN');
  await assertRoutes(page, [
    '/admin/dashboard', '/admin/students', '/admin/students/new', '/admin/professors',
    '/admin/subjects', '/admin/programs', '/admin/school-years', '/admin/exam-periods',
    '/admin/exams', '/admin/exams/1/results', '/admin/year-enrollments'
  ]);
  expect(errors).toEqual([]);
});

test('STUDENT key routes render without runtime errors', async ({ page }) => {
  const errors = await mockRole(page, 'STUDENT');
  await assertRoutes(page, [
    '/student/dashboard', '/student/profile', '/student/subjects', '/student/exams',
    '/student/grades', '/student/year-enrollment'
  ]);
  expect(errors).toEqual([]);
});

test('PROFESSOR key routes render without runtime errors', async ({ page }) => {
  const errors = await mockRole(page, 'PROFESSOR');
  await assertRoutes(page, [
    '/professor/dashboard', '/professor/subjects', '/professor/subjects/1/students',
    '/professor/exams', '/professor/exams/1/registered', '/professor/exams/1/results',
    '/professor/predispit'
  ]);
  expect(errors).toEqual([]);
});
