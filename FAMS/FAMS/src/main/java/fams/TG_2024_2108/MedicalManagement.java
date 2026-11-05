package fams.TG_2024_2108;

import fams.TG_2024_2133.UserManagement;

import fams.TG_2024_2133.UnauthorizedAccessException;
import fams.TG_2024_2133.ValidationException;
import fams.TG_2024_2133.Role;
import fams.TG_2024_2133.UndergraduateUser;
import fams.TG_2024_2133.User;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class MedicalManagement {
    private final MedicalDAO medicalDAO;

    public MedicalManagement() {
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
        UserManagement.getInstance().requireRole(Role.UNDERGRADUATE, Role.TECHNICAL_OFFICER, Role.ADMIN);
        User currentUser = UserManagement.getInstance().getCurrentUser();

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
        UserManagement.getInstance().requireRole(Role.TECHNICAL_OFFICER, Role.ADMIN);
        User currentUser = UserManagement.getInstance().getCurrentUser();
        String reviewerName = (currentUser != null) ? currentUser.getFullName() : "System TO";

        MedicalRecord existing = medicalDAO.getMedicalById(medicalId);
        if (existing == null) {
            throw new ValidationException("Medical record #" + medicalId + " was not found.");
        }
        medicalDAO.updateMedicalStatus(medicalId, newStatus, reviewerName);
    }

    public List<MedicalRecord> getMedicalsForCurrentUser() {
        User currentUser = UserManagement.getInstance().getCurrentUser();
        if (currentUser == null) return List.of();

        if (currentUser.getRole() == Role.UNDERGRADUATE) {
            String regNo = ((UndergraduateUser) currentUser).getStudentRegNo();
            return medicalDAO.getMedicalsByStudent(regNo);
        } else {
            return medicalDAO.getAllMedicals();
        }
    }
}
