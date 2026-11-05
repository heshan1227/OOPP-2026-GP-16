# Lecturer Student Profile Feature

Added a read-only **Student Profiles** screen for users with the `LECTURER` role.

## What it does
- Adds a `Student Profiles` item to the lecturer navigation sidebar only.
- Lists undergraduate students using the existing `UserDAO` and existing `users` data.
- Supports searching by registration number, name, batch, department, email, and phone.
- Shows the selected student's registration number, name, batch, department, email, phone, repeat status, and batch-missed status.
- Refreshes the student list whenever the screen is opened.
- Does not add, update, or delete student records and does not require a database schema change.

## Files changed
- `src/com/fams/gui/MainFrame.java` — lecturer-only navigation, screen registration, and refresh hook.
- `src/com/fams/gui/StudentProfilePanel.java` — new read-only student profile directory.

The feature uses the existing `UserDAO.getAllUsers()` method and the existing `UndergraduateUser` model. No existing screens or database tables were modified.
