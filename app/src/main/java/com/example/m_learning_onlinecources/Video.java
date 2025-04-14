package com.example.m_learning_onlinecources;

public class Video {
    private String title;
    private String url;

    // Constructor
    public Video(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // Empty constructor for Firebase
    public Video() {}

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
