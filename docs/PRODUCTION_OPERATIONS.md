# Production operations

## Secrets

Database credentials, CORS origins, exchange-rate configuration and finance limits must be supplied through environment variables. Do not commit production values.

## Backup

Run `ops/backup-mysql.ps1 -OutputFile <path>` with `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` and `DB_PASSWORD` set. Store encrypted backups outside the application host and verify checksums.

## Restore drill

Restore only into an isolated empty database:

`ops/restore-mysql.ps1 -InputFile <path> -ConfirmRestore`

After restore, start the application with Flyway validation enabled, run smoke tests, compare row counts, and record the drill date. Perform a restore drill at least quarterly.

## Migration rollback

Flyway migrations are additive. Before deployment, create a backup and test the migration on a production-like copy. Rollback means restoring that backup and deploying the previous application version; never edit an already applied migration.

## Monitoring

Monitor `/actuator/health`, HTTP 5xx and 429 rates, login failures, database pool saturation, document storage capacity, and exchange-rate fallback usage. Propagate `X-Correlation-ID` through reverse proxy and logs.
