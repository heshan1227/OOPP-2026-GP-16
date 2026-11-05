# FAMS Integrated Project — Group B16

Faculty Academic Management System (FAMS), University of Ruhuna — ICT2132 OOP Practicum.

This integrated project brings the four submitted codebases into one Java Swing application and one MySQL schema. It retains the original responsibility split and provides shared source so every member can open and run the same project.

## Integrated modules

- **Authentication and User Management** — login, role-based dashboards, admin user CRUD.
- **Course & Academic Management** — course CRUD, course materials, undergraduate enrollment.
- **Marks & Eligibility** — assessment creation, marks entry/update (0–100), CA weighted values in the marks table.
- **Attendance** — theory/practical attendance and percentages.
- **Medical Records** — students, Technical Officers and Admin upload medicals through the same system (no per-subject upload limit; duplicate active session submissions are blocked); Technical Officers/Admin review them in a separate **Approve / Reject / View Medical** tab. Approving or rejecting a medical updates the Attendance screens automatically. See `MEDICAL_ATTENDANCE_FIX.md`.
- **Notices** — publish and view notices by role.
- **Timetables** — timetable management and viewing.

## Open in IntelliJ IDEA

Extract the ZIP and open the extracted project folder (the folder containing `pom.xml`) using **File → Open**. A shared **FAMS Main** run configuration is included under `.run`. See `RUN_IN_INTELLIJ.md` for exact steps. Select JDK 17 or newer and allow Maven to download dependencies. MySQL must be configured separately for database-backed features.

## Requirements

- JDK 17 or newer
- MySQL 8.x
- Maven 3.8+ (recommended)
- MySQL Connector/J is declared in `pom.xml`

## First-time setup

1. Start MySQL.
2. Open `src/main/resources/sql/FAMS_complete_database_setup_FIXED_v2.sql` in MySQL Workbench or phpMyAdmin and execute it. It creates `fams_db`, core tables and the integrated assessment/marks/enrollment/material tables.
3. The default connection is `localhost:3306`, database `fams_db`, user `root`, password `1234` (edit `src/main/resources/database.properties` or set `FAMS_DB_PASSWORD` if your local MySQL root password differs).
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
   java -jar target/fams-integrated-1.0.0.jar   # main class: fams.Main
   ```

## Demo data and credentials

`src/main/resources/sql/FAMS_complete_database_setup_FIXED_v2.sql` includes sample users, current-semester courses, generated attendance coverage across seeded undergraduates and course components, medical scenario records and assessment templates. Seed accounts use classroom-only sample credentials in the SQL seed section. Do not use these accounts or credentials for a real deployment. Password hashing and credential migration must be completed before real student data is used.

The Marks & Eligibility panel and `MarksService`/`MarksDAO` provide database-based eligibility queries and grade/SGPA calculation utilities. A role-aware **My Profile** screen is available to all users; undergraduate profile edits are restricted to contact fields and profile-picture path, while lecturer/TO profiles can edit other profile details but not username/password. The undergraduate **My Student Portal** provides personal attendance, medical, enrolled-course, marks and timetable tabs. The profile picture field is stored as a path/URL; image upload and image rendering are not included. **Important:** `GradeCalculator` thresholds are an implementation template and have not been verified against the official UGC Circular 12-2024 grade table. Confirm the official grade bands before academic use. A complete CGPA/transcript UI and full integrated runtime acceptance testing remain to be verified.

The application is primarily MySQL-backed for marks, courses, materials and enrollments; selected older modules retain their in-memory demo fallback. Keep demo data separate from real student records.

## Important validation notes

- Use a clean `fams_db` for first-time setup; the SQL includes foreign-key and profile-column changes that may require a migration if you already created older tables. Back up any existing database before rebuilding.
- Assessment weightings are demonstration templates, not official course-coordinator criteria. Replace them with the actual semester evaluation criteria before entering real marks.
- The SQL attendance/medical rows are synthetic demonstration data and must not be treated as actual student records.
- Java source compilation was checked with `javac`; a Maven package build and end-to-end MySQL GUI test were not run in this environment.

## Connectivity troubleshooting

- Ensure MySQL is running and `fams_db` exists.
- Check `FAMS_DB_USER` and `FAMS_DB_PASSWORD`.
- Ensure Connector/J is present (Maven includes it in the packaged JAR).
- If a port other than 3306 is used, update `FAMS_DB_URL`.
- Execute the schema on a clean database first. For an existing database, back it up and review the `CREATE TABLE` statements before migrating.
- Avoid running the SQL script repeatedly on a database containing important records without a backup.

## Source layout

```
FAMS/
├── pom.xml
├── src/main/java/fams/
│   ├── Main.java
│   ├── TG_2024_2133/   Users & Admin        (UserManagement, AdminManagement)
│   ├── TG_2024_2138/   Courses & Academic   (CourseManagement, AcademicManagement)
│   ├── TG_2024_2113/   Marks & GPA          (MarksManagement, GPAManagement)
│   └── TG_2024_2108/   Attendance, Medical, Notices, Timetable
│                       (AttendanceManagement, MedicalManagement, NoticeManagement, TimetableManagement)
├── src/main/resources/
│   ├── database.properties          DB url/user/password (env vars FAMS_DB_* override)
│   └── sql/                         MySQL schema + sample data
├── src/test/java/fams/TG_2024_2113/ AcademicResultEngineTest
├── docs/                            feature notes and audit documents
└── uploads/avatars/                 profile pictures (runtime)
```

Each member package contains the `*Management` classes named above (business logic / services) plus that
member's DAOs, models and Swing panels. Shared infrastructure that every module depends on lives in
`TG_2024_2133`: `DBConnection`, `DatabaseInitializer`, the exception classes, the `User` hierarchy and `Role`,
`Theme`, `LoginFrame` and `MainFrame`.

| Package | Management classes | Other classes |
|---|---|---|
| `TG_2024_2133` | `UserManagement` (login, session, role checks), `AdminManagement` (user CRUD) | `User`, `Role`, `*User`, `UserDAO`, `LoginFrame`, `MainFrame`, `ProfilePanel`, `StudentProfilePanel`, `StudentPortalPanel`, `UserManagementPanel`, `DashboardHomePanel`, `Theme`, `DBConnection`, `DatabaseInitializer`, exceptions |
| `TG_2024_2138` | `CourseManagement` (courses, materials), `AcademicManagement` (enrolment) | `CourseManagementPanel`, `CourseListPanel` |
| `TG_2024_2113` | `MarksManagement`, `GPAManagement` (grade bands, SGPA) | `AcademicResultEngine`, `ResultsAccessPolicy`, `MarksDAO`, `AcademicResultsDAO`, `GradeResult`, `ComponentType`, `MarksManagementPanel` |
| `TG_2024_2108` | `AttendanceManagement`, `MedicalManagement`, `NoticeManagement`, `TimetableManagement` | DAOs, models, panels, `EmailQueueWorker` |

Prepare an individual contribution page for each actual group member, describing only work they personally completed and can demonstrate (see `docs/GROUP_CONTRIBUTIONS.md`).

## Marks & Exam Eligibility Upgrade

For the upgraded PDF-aligned marks and eligibility module, follow `MARKS_ELIGIBILITY_V2_GUIDE.md` and import `marks_eligibility_v2.sql` **after** `src/main/resources/sql/FAMS_complete_database_setup_FIXED_v2.sql`. The migration seeds the ICT101-ICT103 sample marks and the S001-S003 example students. Do not skip the migration: the new DAO and student-approved-results query use the `academic_*` tables it creates.


## Java version requirement (corrected)

This project uses Java 17 language features (including records and pattern matching). Use JDK 17 or newer and import the project from `pom.xml` as a Maven project. The Maven compiler is configured with `maven.compiler.release=17`. In IntelliJ IDEA, set the Project SDK and Maven runner JDK to JDK 17 or newer, then reload the Maven project.

Database classes were corrected to compile together: `MarksService` now has its required JDBC/BigDecimal/Optional imports, and the duplicate quiz-count check that referenced a connection before it was opened has been removed (the check remains inside the database transaction).
