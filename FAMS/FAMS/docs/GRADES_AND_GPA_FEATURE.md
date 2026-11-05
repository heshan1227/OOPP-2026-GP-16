# FAMS Marks & Exam Eligibility Upgrade (v2)

This update intentionally keeps the existing Swing screens and overall layout. It replaces the marks/grade calculation path, adds the academic-results schema, changes lecturer mark entry/publication to the new transactional tables, and prevents the undergraduate portal from querying raw assessment marks.

## Installation

1. Back up your existing `fams_db` database.
2. Import the existing `database_schema.sql` first (only if the base FAMS schema has not already been imported).
3. Open MySQL Workbench or the MySQL command line and run `marks_eligibility_v2.sql` against `fams_db`.
4. Open the project in IntelliJ and reload the Maven project.
5. Set the MySQL connection using `FAMS_DB_URL`, `FAMS_DB_USER`, and `FAMS_DB_PASSWORD` environment variables (or matching Java system properties).
6. Run `com.fams.Main`.

The migration is additive: it does not remove the attendance, medical, notice, timetable, or existing user tables. It adds the new academic tables and sample data. It creates example users S001-S003 with the demo password `student123`; change demo passwords before using this outside a local assignment environment.

## Implemented marks and eligibility rules

- Grade mapping follows the supplied boundaries exactly: A+ 85, A 75, A- 70, B+ 65, B 60, B- 55, C+ 50, C 45, C- 40, D 35, E below 35.
- Grade points: A+/A 4.0, A- 3.7, B+ 3.3, B 3.0, B- 2.7, C+ 2.3, C 2.0, C- 1.7, D 1.3, E 0.0.
- Marks and calculated final marks use decimal arithmetic and HALF_UP rounding to two places.
- CA pass threshold defaults to 40; ESA pass threshold defaults to 35. A failed component produces E(CA) or E(ESA) and zero GP.
- CA/ESA split and pass thresholds are stored per module in `module_result_config`; 100% CA is supported.
- SGPA is credit-weighted within a semester. CGPA averages semester SGPAs equally (uniform semester contribution), not by combining all semester credits into one pool.
- N and W are excluded from GPA. Non-GPA English modules are stored with `gpa_module=FALSE`.
- Repeat attempts are stored separately using `attempt_no`; `is_best` identifies the selected attempt. `AcademicResultEngine.bestAttempt(...)` provides best-grade selection logic.
- Exam eligibility uses CGPA >= the configured threshold (default 2.00). Each check inserts a new row in `eligibility_snapshots`. Database triggers prevent update/delete of snapshot rows.
- End-exam mark saving checks the latest eligibility snapshot for that student and semester. NOT_ELIGIBLE blocks ESA entry unless an Admin override snapshot with a reason exists.
- The semester-5 progression gate is separate from exam eligibility and uses the first-three-semester CGPA.
- Technical Officers are blocked from marks/results at the Swing navigation, service facade, and DAO entry points. Undergraduate grade queries are scoped to the authenticated user's own user ID and approved best-attempt rows only. The student portal no longer queries raw CA/ESA marks.

## Lecturer and Admin workflow

- Lecturer assessment creation and mark entry use `academic_assessments` and `academic_marks`.
- CA quizzes are limited to quiz numbers 1-3; the DAO validates the count and the database has a CHECK constraint.
- `AcademicResultsDAO.saveBulkMarks(...)` saves a whole map of student marks in one SQL transaction. Any validation/enrolment/eligibility error rolls the entire batch back.
- Lecturer publication creates a `SUBMITTED` publish request. Admin approval recalculates the affected course/semester results, publishes only those results, appends eligibility snapshots, and queues in-app/email notifications. Rejection is supported by `reviewPublishRequest(..., false, reason)`.
- `email_queue` is populated in the request transaction. `EmailQueueWorker` is provided as a background dispatcher; deployment must supply an SMTP-backed `MailSender` and start the worker. No SMTP credentials or mail provider were assumed.

## Seed examples

The migration adds:
- ICT101, ICT102 and ICT103 with the supplied credits and component marks for S001-S003.
- Y1S1 and Y1S2; S001 has 8 GPA credits in Y1S1 and 12 in Y1S2 to test uniform semester weighting.
- 3 CA quizzes for ICT101.
- Example publish requests in DRAFT, SUBMITTED, APPROVED and REJECTED states.
- N, W, English non-GPA, repeat-attempt/advisory and semester-5 progression examples.
- Eligibility snapshots for the supplied expected SGPA examples.

## Verification

- Main Java sources compile with Java 17+.
- `test/com/fams/service/AcademicResultEngineTest.java` contains JUnit 5 tests for grade boundaries, half-up rounding, CA/ESA pass thresholds, N/W and non-GPA exclusion, best attempt, eligibility threshold, class cut-offs, and uniform-semester CGPA.
- Maven tests can be run with `mvn test`. Maven was not installed in the build environment used to prepare this archive, so the JUnit suite could not be executed here. The main Java source tree was compiled with `javac`, and calculation smoke tests passed.

## Scope notes

This is a Swing desktop application, not a web application with REST controllers. The role checks therefore sit in the service/DAO boundary rather than HTTP middleware. The existing screen layout has not been redesigned. The database and service layer now provide transactional batch saving and the publish/eligibility workflow; a CSV/Excel preview importer, PDF transcript/report renderer, full notification center UI, and SMTP provider configuration still require integration if those are required in the final deployment.
