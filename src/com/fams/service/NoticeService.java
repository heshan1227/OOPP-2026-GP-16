package com.fams.service;

import com.fams.database.NoticeDAO;
import com.fams.exception.UnauthorizedAccessException;
import com.fams.exception.ValidationException;
import com.fams.model.Notice;
import com.fams.model.Role;
import com.fams.model.User;

import java.util.List;

public class NoticeService {
    private final NoticeDAO noticeDAO;

    public NoticeService() {
        this.noticeDAO = new NoticeDAO();
    }

    public void publishNotice(Notice notice) throws ValidationException, UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.ADMIN);

        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            throw new ValidationException("Notice title cannot be empty.");
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            throw new ValidationException("Notice content cannot be empty.");
        }

        noticeDAO.saveNotice(notice);
    }

    public void deleteNotice(int noticeId) throws UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.ADMIN);
        noticeDAO.deleteNotice(noticeId);
    }

    public List<Notice> getNoticesForCurrentSession() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null) return List.of();

        return noticeDAO.getNoticesForRole(currentUser.getRole());
    }
}
