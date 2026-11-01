package com.fams.service;

import com.fams.database.MedicalDAO;
import com.fams.exception.UnauthorizedAccessException;
import com.fams.exception.ValidationException;
import com.fams.model.MedicalRecord;
import com.fams.model.MedicalStatus;
import com.fams.model.Role;
import com.fams.model.UndergraduateUser;
import com.fams.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalService {
    /** A student may have at most this many pending/approved medicals per course (subject). */
    public static final int MAX_MEDICALS_PER_COURSE = 3;

    private final MedicalDAO medicalDAO;

    public MedicalService() {
        this.medicalDAO = new MedicalDAO();
    }

    /** Submit a single medical. Student, Technical Officer and Admin can use the same upload system. */
    public void submitMedical(MedicalRecord record) throws ValidationException, UnauthorizedAccessException {
        submitMedicals(List.of(record));
    }

    /**
     * Submit one medical per selected absent session. Every rule is checked before anything is saved,
     * so either all records are stored or none of them.
     */
    public void submitMedicals(List<MedicalRecord> records) throws ValidationException, UnauthorizedAccessException {
        validateSubmission(records);
        for (MedicalRecord record : records) {
            medicalDAO.saveMedical(record);
        }
    }

    /** Runs all submission rules without saving anything (used by the UI before copying the certificate image). */
    public void validateSubmission(List<MedicalRecord> records) throws ValidationException, UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.UNDERGRADUATE, Role.TECHNICAL_OFFICER, Role.ADMIN);
        User currentUser = AuthService.getInstance().getCurrentUser();

        if (records == null || records.isEmpty()) {
            throw new ValidationException("Select at least one absent session for the medical.");
        }

        // key = student|course  ->  number of new medicals requested in this submission
        Map<String, Integer> requested = new LinkedHashMap<>();
        Map<String, String> sessionKeys = new LinkedHashMap<>();

        for (MedicalRecord record : records) {
            if (record.getStudentRegNo() == null || record.getStudentRegNo().trim().isEmpty()) {
                throw new ValidationException("Student Registration Number is required.");
            }
            if (currentUser instanceof UndergraduateUser
                    && !((UndergraduateUser) currentUser).getStudentRegNo().equalsIgnoreCase(record.getStudentRegNo())) {
                throw new UnauthorizedAccessException("Students can only submit medicals for themselves.");
            }
            if (record.getReferenceNo() == null || record.getReferenceNo().trim().isEmpty()) {
                throw new ValidationException("Medical Reference Number is required.");
            }
            if (record.getReason() == null || record.getReason().trim().isEmpty()) {
                throw new ValidationException("Reason for absence is required.");
            }

            String sessionKey = record.getStudentRegNo() + "|" + record.getCourseCode() + "|"
                    + record.getComponentType() + "|" + record.getSessionNumber();
            if (sessionKeys.put(sessionKey, sessionKey) != null
                    || medicalDAO.hasActiveMedicalForSession(record.getStudentRegNo(), record.getCourseCode(),
                    record.getComponentType(), record.getSessionNumber())) {
                throw new ValidationException("A medical has already been submitted for "
                        + record.getCourseCode() + " " + record.getComponentType().getDisplayName()
                        + " session " + record.getSessionNumber() + ".");
            }

            requested.merge(record.getStudentRegNo() + "|" + record.getCourseCode(), 1, Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : requested.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            int used = medicalDAO.countActiveMedicals(parts[0], parts[1]);
            if (used + entry.getValue() > MAX_MEDICALS_PER_COURSE) {
                int remaining = Math.max(0, MAX_MEDICALS_PER_COURSE - used);
                throw new ValidationException("Medical limit reached for " + parts[1] + ": only "
                        + MAX_MEDICALS_PER_COURSE + " medicals are allowed per subject. "
                        + parts[0] + " has already used " + used + " and can submit " + remaining
                        + " more, but " + entry.getValue() + " were selected.");
            }
        }
    }

    /** Number of medical slots (pending + approved) already used by the student for a course. */
    public int getUsedMedicalCount(String studentRegNo, String courseCode) {
        return medicalDAO.countActiveMedicals(studentRegNo, courseCode);
    }

    public void updateApprovalStatus(int medicalId, MedicalStatus newStatus) throws UnauthorizedAccessException, ValidationException {
        AuthService.getInstance().requireRole(Role.TECHNICAL_OFFICER, Role.ADMIN);
        User currentUser = AuthService.getInstance().getCurrentUser();
        String reviewerName = (currentUser != null) ? currentUser.getFullName() : "System TO";

        MedicalRecord existing = medicalDAO.getMedicalById(medicalId);
        if (existing == null) {
            throw new ValidationException("Medical record #" + medicalId + " was not found.");
        }
        // A rejected medical does not use a slot, so re-opening it must respect the 3-per-subject limit.
        if (existing.getStatus() == MedicalStatus.REJECTED && newStatus != MedicalStatus.REJECTED
                && medicalDAO.countActiveMedicals(existing.getStudentRegNo(), existing.getCourseCode()) >= MAX_MEDICALS_PER_COURSE) {
            throw new ValidationException("Cannot " + newStatus.getDisplayName().toLowerCase() + " this medical: "
                    + existing.getStudentRegNo() + " already has " + MAX_MEDICALS_PER_COURSE
                    + " active medicals for " + existing.getCourseCode() + ".");
        }

        medicalDAO.updateMedicalStatus(medicalId, newStatus, reviewerName);
    }

    public List<MedicalRecord> getMedicalsForCurrentUser() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null) return List.of();

        if (currentUser.getRole() == Role.UNDERGRADUATE) {
            String regNo = ((UndergraduateUser) currentUser).getStudentRegNo();
            return medicalDAO.getMedicalsByStudent(regNo);
        } else {
            return medicalDAO.getAllMedicals();
        }
    }
}
