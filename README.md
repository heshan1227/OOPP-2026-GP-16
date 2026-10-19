# FAMS Integrated Project — Group B16

Faculty Academic Management System (FAMS), University of Ruhuna — ICT2132 OOP Practicum.

This integrated project brings the four submitted codebases into one Java Swing application and one MySQL schema. It retains the original responsibility split and provides shared source so every member can open and run the same project.

## Integrated modules

- **Authentication and User Management** — login, role-based dashboards, admin user CRUD.
- **Course & Academic Management** — course CRUD, course materials, undergraduate enrollment.
- **Marks & Eligibility** — assessment creation, marks entry/update (0–100), CA weighted values in the marks table.
- **Attendance** — theory/practical attendance and percentages.
- **Medical Records** — submission and approval state.
- **Notices** — publish and view notices by role.
- **Timetables** — timetable management and viewing.

## Requirements

- JDK 17 or newer
- MySQL 8.x
- Maven 3.8+ (recommended)
- MySQL Connector/J is declared in `pom.xml`

## First-time setup

1. Start MySQL.
2. Open `database_schema.sql` in MySQL Workbench or phpMyAdmin and execute it. It creates `fams_db`, core tables and the integrated assessment/marks/enrollment/material tables.
3. The default connection is `localhost:3306`, database `fams_db`, user `root`, blank password.
4. If your MySQL credentials differ, set environment variables before launching:
   - `FAMS_DB_URL` (optional; default JDBC URL is in `DBConnection.java`)
   - `FAMS_DB_USER`
   - `FAMS_DB_PASSWORD`
5. Build from this folder:
   ```bash
   mvn clean package
   ```
6. Run:
   ```bash
   java -jar target/fams-integrated-1.0.0.jar
   ```

## Demo login

The SQL script seeds:
- Username: `admin`
- Password: `admin123`

Change the demo password before using real or sensitive data. The original project schema stores passwords as plain text for compatibility with the submitted authentication code; this is a classroom demo baseline, not production-grade authentication.

If the database is not available, some original modules can use the included in-memory demo data. User management, materials, enrollment and marks require MySQL. The status bar indicates whether the application detected the MySQL connection.

## Important data conventions

- Student IDs for the sample in-memory dataset are `UG1`, `UG2`, etc. When using the database, use the `users.user_id` value for the undergraduate account (the seed/admin user is `ADM001`).
- Course codes must already exist before adding assessments, materials or enrollments.
- Assessment marks must be between 0 and 100. Assessment weight is validated between 0 and 100.
- The SRS requires the official UGC Circular No. 12-2024 grade-point mapping. Do not treat example thresholds from the earlier TG2113 contribution as official. Grade/SGPA/CGPA mapping remains a follow-up integration item and should be configured only after checking the authoritative circular.
- Approved medical treatment for attendance eligibility is faculty-policy dependent and is not silently assumed here.

## Connectivity troubleshooting

- Ensure MySQL is running and `fams_db` exists.
- Check `FAMS_DB_USER` and `FAMS_DB_PASSWORD`.
- Ensure Connector/J is present (Maven includes it in the packaged JAR).
- If a port other than 3306 is used, update `FAMS_DB_URL`.
- Execute the schema on a clean database first. For an existing database, back it up and review the `CREATE TABLE` statements before migrating.
- Avoid running the SQL script repeatedly on a database containing important records without a backup.

## Source layout

`src/com/fams/` is the shared package tree:
- `model/` — domain objects and role types
- `database/` — JDBC connection, DAOs, initialization/schema-related code
- `service/` — validation and application services
- `gui/` — Swing login, dashboard and module screens
- `exception/` — application exceptions

Each member archive contains this same runnable project plus a contribution note to simplify individual work and review.
