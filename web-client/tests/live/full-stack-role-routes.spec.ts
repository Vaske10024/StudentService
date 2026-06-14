import { expect, Page, test } from '@playwright/test';

const rolePassword = 'E2E-Role-Password-123!';

async function login(page: Page, username: string, password: string) {
  await page.goto('/login');
  await page.getByLabel('Username or email').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Login' }).click();
  await expect(page).toHaveURL(/\/dashboard$/);
}

async function assertRoutes(page: Page, routes: string[]) {
  for (const route of routes) {
    await page.goto(route);
    await expect(page.locator('h1').first()).toBeVisible();
    await expect(page).not.toHaveURL(/\/login$/);
  }
}

test('live ADMIN login, routes, and study-type mutation use the Spring backend', async ({ page }) => {
  await login(page, 'e2e.admin@example.test', 'E2E-Admin-Password-123!');
  await assertRoutes(page, ['/admin/dashboard', '/admin/students', '/admin/programs', '/admin/school-years']);

  await page.goto('/admin/programs');
  await page.getByRole('button', { name: 'New study type' }).click();
  const code = `E2E${Date.now()}`;
  const dialog = page.getByRole('dialog', { name: 'New study type' });
  await dialog.getByLabel('Code *').fill(code);
  await dialog.getByLabel('Full name *').fill('Live E2E study type');
  await dialog.getByRole('button', { name: 'Save study type' }).click();
  await expect(page.getByRole('cell', { name: code, exact: true })).toBeVisible();
});

test('live STUDENT login renders real backend pages', async ({ page }) => {
  await login(page, 'e2e.student@example.test', rolePassword);
  await assertRoutes(page, ['/student/dashboard', '/student/profile', '/student/subjects', '/student/exams', '/student/year-enrollment']);
});

test('live PROFESSOR login renders real backend pages', async ({ page }) => {
  await login(page, 'e2e.professor@example.test', rolePassword);
  await assertRoutes(page, ['/professor/dashboard', '/professor/subjects', '/professor/exams', '/professor/predispit']);
});
