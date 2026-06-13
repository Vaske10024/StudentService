# ServerAndDTOs

Spring Boot 2.2 / Java 11 multi-module backend retained as the core REST API for the migrated web application.

Run the complete backend test suite with the included Maven wrapper:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Tests use `server/src/test/resources/application-test.properties` with H2 in MySQL compatibility mode.

Before running only the `server` module, install the current reactor artifacts so
Maven does not use an older `dtos` snapshot:

```powershell
.\mvnw.cmd install -DskipTests
.\mvnw.cmd -pl server spring-boot:run
```

See `../README.md` for full setup, security, role matrix, migration notes, and frontend instructions.
