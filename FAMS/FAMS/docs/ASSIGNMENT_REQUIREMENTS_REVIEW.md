# ICT2132 Mini Project – Requirements Review

This review is based only on the uploaded four-page ICT2132 assignment brief. It does not assume grading details that the brief does not provide.

## Requirements represented in the current codebase
- Four roles are represented: Admin, Lecturer, Technical Officer and Undergraduate.
- User, course, attendance, medical, notice, timetable, course-material, assessment and marks data structures are present.
- The database initializer defines 1 admin, 5 lecturers, 4 technical officers and 20 undergraduates.
- Attendance initialization describes 15 theory and 15 practical sessions and seeds the five attendance/medical scenarios listed in the brief.
- Attendance service supports theory/practical and combined calculations; marks eligibility logic checks CA >= 40% and attendance >= 80%.
- Role-specific navigation and student read-only portal views are present.

## Items requiring confirmation or further implementation before claiming full compliance
1. **UGC Circular No. 12-2024 grade scale:** the assignment brief names the circular but does not reproduce its grade boundaries or grade-point mapping. The existing `GradeCalculator` explicitly says its thresholds must be checked against the approved circular. Do not treat those thresholds as verified from the uploaded brief. Obtain the official circular or course-coordinator-approved grading table before changing them.
2. **SGPA and CGPA:** `GradeCalculator` contains an SGPA helper, but the current marks screen does not provide a complete per-student/per-batch grade, SGPA and CGPA report. CGPA calculation/reporting still needs implementation and validation against the approved grading rules.
3. **Whole-batch and individual reporting:** attendance and eligibility summaries exist in parts of the application; verify that the final GUI exposes all requested batch and individual views, including theory-only, practical-only and combined attendance.
4. **Marks evaluation criteria:** the uploaded brief says to follow the current course coordinators' evaluation criteria but does not include those criteria. Assessment weights must be entered from the approved course criteria; they must not be invented.
5. **Attendance time assumptions:** the brief specifies 2 hours per session for each theory credit and 2 hours per session for each practical credit. The current schema stores theory/practical credits, but no complete time-based attendance calculation was verified in this source review.
6. **Medical and attendance consistency:** verify that submitted/approved medicals are reflected consistently in the individual and batch reports and that approval status is visible to the appropriate roles.

## Important data/security note
The latest source config uses a local MySQL password fallback. For deployment, configure `FAMS_DB_URL`, `FAMS_DB_USER` and `FAMS_DB_PASSWORD` as environment variables rather than committing credentials. Seed credentials in the SQL file are demonstration data and should be replaced with secure password hashes before real use.
