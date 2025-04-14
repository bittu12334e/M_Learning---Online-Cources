package com.example.m_learning_onlinecources;
public class Notice {
    private String noticeId;
    private String title;
    private String description;
    private String teacherId;
    private long timestamp;
    private String fileUrl;
    private String fileType;
    public Notice() { }

    public Notice(String noticeId, String title, String description, String teacherId, long timestamp, String fileUrl, String fileType) {
        this.noticeId = noticeId;
        this.title = title;
        this.description = description;
        this.teacherId = teacherId;
        this.timestamp = timestamp;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}
