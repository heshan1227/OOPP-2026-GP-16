package fams.TG_2024_2108;

import fams.TG_2024_2133.UserManagement;

import fams.TG_2024_2133.UnauthorizedAccessException;
import fams.TG_2024_2133.ValidationException;
import fams.TG_2024_2133.Role;
import fams.TG_2024_2133.User;

import java.util.List;

public class NoticeManagement {
    private final NoticeDAO noticeDAO;

    public NoticeManagement() {
        this.noticeDAO = new NoticeDAO();
    }

    public void publishNotice(Notice notice) throws ValidationException, UnauthorizedAccessException {
        UserManagement.getInstance().requireRole(Role.ADMIN);

        if (notice.getTitle() == null || notice.getTitle().trim().isEmpty()) {
            throw new ValidationException("Notice title cannot be empty.");
        }
        if (notice.getContent() == null || notice.getContent().trim().isEmpty()) {
            throw new ValidationException("Notice content cannot be empty.");
        }

        noticeDAO.saveNotice(notice);
    }

    public void deleteNotice(int noticeId) throws UnauthorizedAccessException {
        UserManagement.getInstance().requireRole(Role.ADMIN);
        noticeDAO.deleteNotice(noticeId);
    }

    public List<Notice> getNoticesForCurrentSession() {
        User currentUser = UserManagement.getInstance().getCurrentUser();
        if (currentUser == null) return List.of();

        return noticeDAO.getNoticesForRole(currentUser.getRole());
    }
}
