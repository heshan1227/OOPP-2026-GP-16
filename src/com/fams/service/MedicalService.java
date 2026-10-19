package com.fams.service;

import com.fams.database.MedicalDAO;
import com.fams.exception.UnauthorizedAccessException;
import com.fams.exception.ValidationException;
import com.fams.model.MedicalRecord;
import com.fams.model.MedicalStatus;
import com.fams.model.Role;
import com.fams.model.User;

import java.util.List;

public class MedicalService {
    private final MedicalDAO medicalDAO;

    public MedicalService() {
        this.medicalDAO = new MedicalDAO();
    }

    public void submitMedical(MedicalRecord record) throws ValidationException, UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.UNDERGRADUATE, Role.TECHNICAL_OFFICER, Role.ADMIN);

        if (record.getStudentRegNo() == null || record.getStudentRegNo().trim().isEmpty()) {
            throw new ValidationException("Student Registration Number is required.");
        }
        if (record.getReferenceNo() == null || record.getReferenceNo().trim().isEmpty()) {
            throw new ValidationException("Medical Reference Number is required.");
        }
        if (record.getReason() == null || record.getReason().trim().isEmpty()) {
            throw new ValidationException("Reason for absence is required.");
        }

        medicalDAO.saveMedical(record);
    }

    public void updateApprovalStatus(int medicalId, MedicalStatus newStatus) throws UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.TECHNICAL_OFFICER, Role.ADMIN);
        User currentUser = AuthService.getInstance().getCurrentUser();
        String reviewerName = (currentUser != null) ? currentUser.getFullName() : "System TO";

        medicalDAO.updateMedicalStatus(medicalId, newStatus, reviewerName);
    }

    public List<MedicalRecord> getMedicalsForCurrentUser() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null) return List.of();

        if (currentUser.getRole() == Role.UNDERGRADUATE) {
            String regNo = ((com.fams.model.UndergraduateUser) currentUser).getStudentRegNo();
            return medicalDAO.getMedicalsByStudent(regNo);
        } else {
            return medicalDAO.getAllMedicals();
        }
    }
}
