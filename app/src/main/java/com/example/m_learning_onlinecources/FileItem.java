package com.example.m_learning_onlinecources;

import com.google.firebase.Timestamp;

public class FileItem {
    private String fileId;
    private String fileName;
    private String fileUrl;
    private Timestamp uploadedAt;
    private String fileType;

    // Required empty constructor for Firebase
    public FileItem() {}

    // Updated constructor
    public FileItem(String fileId, String fileName, String fileUrl, Timestamp uploadedAt, String fileType) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.uploadedAt = uploadedAt;
        this.fileType = fileType;
    }
    public FileItem(String fileId, String fileName) {
        this.fileId = fileId;
        this.fileName = fileName;

    }

    // Getter and Setter for id
    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    // Getters and setters for fileName
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    // Getters and setters for fileUrl
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    // Getters and setters for uploadedAt
    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // Getters and setters for fileType
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
}
