import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests/live',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: 'list',
  use: {
    baseURL: 'http://127.0.0.1:5174',
    trace: 'retain-on-failure'
  },
  webServer: [
    {
      command: 'powershell -NoProfile -ExecutionPolicy Bypass -File tests/live/start-backend.ps1',
      url: 'http://127.0.0.1:8081/api/auth/csrf',
      reuseExistingServer: false,
      timeout: 240_000
    },
    {
      command: 'powershell -NoProfile -ExecutionPolicy Bypass -File tests/live/start-frontend.ps1',
      url: 'http://127.0.0.1:5174',
      reuseExistingServer: false,
      timeout: 120_000
    }
  ]
});
