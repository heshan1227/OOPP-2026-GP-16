# Medical <-> Attendance fix

## 1. Approved medical now updates Attendance
* `MedicalDAO` now syncs the matching `attendance_records` row whenever a medical is uploaded / approved / rejected
  (`AttendanceDAO.updateMedicalFlags`). Works for MySQL and for the in-memory fallback.
  * Pending  -> `has_medical_submitted = TRUE`, `is_medical_approved = FALSE`
  * Approved -> `has_medical_submitted = TRUE`, `is_medical_approved = TRUE`  (shows **Absent (Approved Medical)**)
  * Rejected -> both `FALSE` (the absence goes back to a plain absence and the session can get a new medical)
* **Attendance Log (TO)** shows the new status; **My Daily Attendance** (student) now shows *Absent (Medical Approved / Pending)*
  and counts approved-medical hours as participated; **Individual** and **Batch** summaries have an
  *Attendance incl. Approved Medicals* value.
* Attendance and Medical screens reload every time you open them from the sidebar (`MainFrame.showCard`).
* Existing MySQL database: run `sync_medical_attendance.sql` once so old approved medicals are reflected.

## 2. Medical upload limit
There is no longer a per-subject upload cap. Duplicate active medical submissions for the same student, subject, component, and session are still blocked.
The same absent session cannot get two medicals. The upload screen shows e.g. `ICT2132 2/3 (+1 selected)`.
Sample data was adjusted so no student has more than 3 medicals per subject.

## 3. Admin / Technical Officer upload + 2 tabs
Medical Records screen for Admin and Technical Officer:
* **Upload Medical** - choose a student, date range, tick the absent sessions, reason + certificate photo.
* **Approve / Reject / View Medical** - status filter, View Medical (shows the real uploaded image), Approve, Reject.

Students keep the upload form + their own history (with View Certificate). Lecturers get the read-only review table.
