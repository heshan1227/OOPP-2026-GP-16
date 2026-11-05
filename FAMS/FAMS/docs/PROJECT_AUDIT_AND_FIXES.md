# Project audit and fixes

## Applied fixes

1. **Prevented accidental database deletion.** `database_schema.sql` previously began with `DROP DATABASE IF EXISTS fams_db`, which erased all existing users, attendance, medical records, marks, and other data whenever the setup script was run. It now uses `CREATE DATABASE IF NOT EXISTS` and preserves existing data.
2. **Hardened undergraduate lookup.** `UserDAO.findUndergraduateByRegNo` now handles missing registration numbers safely instead of risking a `NullPointerException`.
3. **Hardened fallback authentication.** The in-memory login check now safely handles a null password, and database role values are normalized before enum conversion.

## Verification

- Compiled all 51 Java source files with Java 21 using `javac -Xlint:all`: **successful**.
- The compiler reported warnings (mostly Swing serialization IDs and Swing constructor `this`-escape warnings); no Java compilation errors were reported.
- A live MySQL integration test could not be run in this environment. Please test database-backed login and CRUD operations using your own MySQL instance.

## Important remaining deployment concerns

- The project currently stores and compares passwords as plain text, including demo credentials. Do not use real student/staff passwords until password hashing (for example BCrypt or Argon2) is implemented.
- `CREATE TABLE IF NOT EXISTS` does not update the structure of tables that already exist. If your database is older, apply reviewed migrations after taking a backup; do not drop the database to resolve column errors.
- The sample accounts and sample records are for development/demo use only.
