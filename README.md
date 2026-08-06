# MF-SYS Backup Service

A Spring Boot backend service for automated backup, restore, and data cleanup of the databases behind MFSYS's core banking platform. Supports both **MySQL** and **MongoDB**, and was built to run scheduled, hands-off backups across multiple client deployments rather than relying on manual dumps.

Built collaboratively by [Ghulam Ali](https://github.com/ali38241) and [Eid Muhammad](https://github.com/eido38990) during our time at MFSYS Technologies.

## What it does

- **Backup** — triggers `mysqldump` / `mongodump` for one or more named databases, writes the output into a timestamped folder (`MM-dd-yyyy__HH-mm-ss`), and automatically prunes backups older than 14 days to keep disk usage under control.
- **Restore** — restores a chosen database from any previous backup date using `mysql` / `mongorestore`.
- **Download as ZIP** — streams a given backup folder back to the caller as a downloadable `.zip`, so backups can be pulled off the server without direct file access.
- **List databases & backups** — endpoints to view all databases on the host, and to browse what's already been backed up on disk.
- **Host configuration** — DB host/user/password are saved to a local config file rather than hardcoded; passwords are encrypted at rest via a small AES utility (`EncryptionUtil`) before being written to disk.
- **Data cleanup by organisation** — since the platform is multi-tenant, `CleanService` can scan every table/collection in a schema and delete (or conversely, retain) rows belonging to a specific organisation code (`POR_ORGACODE`), across both MySQL and MongoDB.

## Tech stack

- Java 17, Spring Boot 3.0.5
- Spring Data JPA / Spring Web
- MySQL (JDBC + `mysqldump`/`mysql` CLI) and MongoDB (Mongo Java driver + `mongodump`/`mongorestore` CLI)
- Gson for config file (de)serialisation
- Native `javax.crypto` (AES) for password encryption

## Project structure

```
src/main/java/com/data/backup/
├── BackupApplication.java      # Spring Boot entry point
├── EncryptionUtil.java         # AES encrypt/decrypt for stored DB passwords
├── app/
│   ├── AppController.java      # Backup / restore / zip / config REST endpoints
│   ├── AppService.java         # Core backup, restore, zip, and host-config logic
│   └── Config.java             # DB host/user/pass/path config model
└── clean/
    ├── CleanController.java    # Data-cleanup REST endpoints
    └── CleanService.java       # Table/collection cleanup by organisation code
```

## Running locally

1. Install MySQL and/or MongoDB locally, and make sure `mysqldump`, `mysql`, `mongodump`, and `mongorestore` are available on the host (the MySQL paths are currently set for a Windows install under `C:\Program Files\MySQL\...` — update `AppService` if your environment differs).
2. Build and run:
   ```
   ./mvnw spring-boot:run
   ```
3. On first run, if no config file exists yet, a dummy config (`localhost` / `root` / `root`) is created automatically at `~/Documents/BackupConfig`. Save real credentials via:
   ```
   POST /sql/saveMysqlHost/{body}
   POST /mongo/saveMongoHost/{body}
   ```

## Known limitations

- The AES key in `EncryptionUtil` is currently hardcoded in source rather than pulled from an environment variable or secrets manager — fine for an internal tool, but would need to change before any production/public use.
- MySQL client paths are hardcoded for a Windows environment.
- No authentication on the API endpoints themselves — access control is assumed to be handled at the network/deployment level.

## Contributors

- **Ghulam Ali** ([@ali38241](https://github.com/ali38241))
- **Eid Muhammad** ([@eido38990](https://github.com/eido38990))
