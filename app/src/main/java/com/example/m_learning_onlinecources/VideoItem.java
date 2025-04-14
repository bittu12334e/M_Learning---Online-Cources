package com.example.m_learning_onlinecources;

import android.net.Uri;

public class VideoItem {
    private String videoTitle;
    private Uri videoUri;
    private String videoId;

    public VideoItem(String videoTitle, Uri videoUri) {
        this.videoTitle = videoTitle;
        this.videoUri = videoUri;
    }
    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }
    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }

    public Uri getVideoUri() {
        return videoUri;
    }
}
