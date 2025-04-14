package com.example.m_learning_onlinecources;

import com.google.firebase.Timestamp;

public class Routine {
    private String id;
    private String fileUrl;
    private String title;
    private Timestamp uploadedAt;
    private String fileType;

    // Required empty constructor for Firestore
    public Routine() {}

    // Getter and Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getFileUrl() {
        return fileUrl;
    }
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Timestamp getUploadedAt() {
        return uploadedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUploadedAt(Timestamp uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
