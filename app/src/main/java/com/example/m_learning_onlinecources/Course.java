package com.example.m_learning_onlinecources;

import java.util.List;
import java.util.Map;

public class Course {
    private String id;
    private String name;
    private String description;
    private String title;
    private String uploadedBy;
    private Map<String, Video> videos;
    private int videoCount; // Dynamically calculated


    public Course() {}

    public Course(String id, String name, String description, String title, String uploadedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.title = title;
        this.uploadedBy = uploadedBy;
    }
    public Course(String id, String name, String description, String title, String uploadedBy, Map<String, Video> videos) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.title = title;
        this.uploadedBy = uploadedBy;
        this.videos = videos;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public Map<String, Video> getVideos() {
        return videos;
    }
    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public static class Video {
        private String title;
        private String url;

        public Video() {}

        public Video(String title, String url) {
            this.title = title;
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public String getUrl() {
            return url;
        }

    }
}
