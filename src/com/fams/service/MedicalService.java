package com.fams.service;

import com.fams.database.MedicalDAO;
import com.fams.exception.UnauthorizedAccessException;
import com.fams.exception.ValidationException;
import com.fams.model.MedicalRecord;
import com.fams.model.MedicalStatus;
import com.fams.model.Role;
import com.fams.model.UndergraduateUser;
import com.fams.model.User;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class MedicalService {
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
        }
    }

    public void updateApprovalStatus(int medicalId, MedicalStatus newStatus) throws UnauthorizedAccessException, ValidationException {
        AuthService.getInstance().requireRole(Role.TECHNICAL_OFFICER, Role.ADMIN);
        User currentUser = AuthService.getInstance().getCurrentUser();
        String reviewerName = (currentUser != null) ? currentUser.getFullName() : "System TO";

        MedicalRecord existing = medicalDAO.getMedicalById(medicalId);
        if (existing == null) {
            throw new ValidationException("Medical record #" + medicalId + " was not found.");
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
