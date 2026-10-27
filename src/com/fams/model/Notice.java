package com.fams.model;

import java.time.LocalDateTime;

public class Notice {
    private int noticeId;
    private String title;
    private String content;
    private String publishedBy; // Admin name
    private LocalDateTime publishedDate;
    private Role targetRole; // null for ALL
    private boolean isImportant;

    public Notice(int noticeId, String title, String content, String publishedBy, LocalDateTime publishedDate, Role targetRole, boolean isImportant) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.publishedBy = publishedBy;
        this.publishedDate = publishedDate;
        this.targetRole = targetRole;
        this.isImportant = isImportant;
    }

    public int getNoticeId() { return noticeId; }
    public void setNoticeId(int noticeId) { this.noticeId = noticeId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }

    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }

    public Role getTargetRole() { return targetRole; }
    public void setTargetRole(Role targetRole) { this.targetRole = targetRole; }

    public boolean isImportant() { return isImportant; }
    public void setImportant(boolean important) { isImportant = important; }

    public String getTargetRoleText() {
        return targetRole == null ? "ALL USERS" : targetRole.getDisplayName().toUpperCase();
    }
}
