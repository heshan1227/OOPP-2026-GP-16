# Attendance and Medical UI update

## Changes
- Medical review heading is now on its own row, with filter/refresh/view/approve/reject controls below it to avoid the overlap shown in the supplied screenshot.
- Admin and Technical Officer retain access to Medical upload and review/approve/reject workflows.
- Attendance maintenance is available to Admin and Technical Officer: save/update a student's session as Present or Absent, view the attendance log, and delete a selected session after confirmation.
- Individual and batch attendance reports now allow selecting the course as well as Theory, Practical, or Combined views.
- Attendance totals use the sessions actually recorded for the selected course instead of always assuming 15 sessions per component. Each Theory (1T) and Practical (1P) session is treated as 2 hours, so combined attendance is calculated on total attended hours / total scheduled hours.
- The existing sample data contains the five requested demonstration cases: above 80% without medicals, exactly 80% without medicals, below 80% without medicals, above 80% with approved medicals, and below 80% with approved medicals.

## Build verification
All Java source files compile successfully with Java 17 using `javac --release 17`.
