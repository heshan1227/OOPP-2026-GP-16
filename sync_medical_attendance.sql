-- Run this ONCE on an existing fams_db if medicals were approved before this fix.
-- It makes attendance_records match medical_records (pending / approved / rejected).
USE fams_db;

UPDATE attendance_records SET has_medical_submitted = FALSE, is_medical_approved = FALSE WHERE record_id > 0;

UPDATE attendance_records ar
JOIN medical_records mr
  ON mr.student_reg_no = ar.student_reg_no AND mr.course_code = ar.course_code
 AND mr.component_type = ar.component_type AND mr.session_number = ar.session_number
SET ar.has_medical_submitted = TRUE,
    ar.is_medical_approved   = (mr.status = 'APPROVED')
WHERE mr.status IN ('PENDING', 'APPROVED') AND ar.is_present = FALSE AND ar.record_id > 0;
