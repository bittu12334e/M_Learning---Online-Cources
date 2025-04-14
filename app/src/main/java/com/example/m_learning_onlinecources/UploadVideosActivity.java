package com.example.m_learning_onlinecources;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class UploadVideosActivity extends AppCompatActivity {

    private VideoAdapter videoAdapter;
    private List<Video> videoList;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private TextView textViewPlaylistTitle;
    private TextView textViewCourseTitle;

    private Button buttonAddVideo, buttonUploadAllVideos;
    private ProgressDialog progressDialog;
    private RecyclerView recyclerViewVideos;
    private String courseId, courseName;
    private static final int MAX_VIDEO_COUNT = 5;
    private int videoCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_videos);

        textViewPlaylistTitle = findViewById(R.id.textViewPlaylistTitle);
        textViewCourseTitle = findViewById(R.id.textViewCourseTitle);
        buttonAddVideo = findViewById(R.id.buttonAddVideo);
        buttonUploadAllVideos = findViewById(R.id.buttonUploadAllVideos);
        recyclerViewVideos = findViewById(R.id.recyclerViewVideos);

        videoList = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoList, this);

        recyclerViewVideos.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewVideos.setAdapter(videoAdapter);

        storageReference = FirebaseStorage.getInstance().getReference("course_videos");
        databaseReference = FirebaseDatabase.getInstance().getReference("courses");

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Video");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        // Get course details from the Intent
        courseId = getIntent().getStringExtra("courseId");
        courseName = getIntent().getStringExtra("courseName");

        // Set course title in the TextView
        if (courseName != null && !courseName.isEmpty()) {
            textViewCourseTitle.setText(courseName);
        } else {
            textViewCourseTitle.setText("Untitled Course");
        }

        buttonAddVideo.setOnClickListener(v -> {
            if (videoCount < MAX_VIDEO_COUNT) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 101);
            } else {
                Toast.makeText(this, "You can only select up to 5 videos.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonUploadAllVideos.setOnClickListener(v -> uploadAllVideos());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                promptForVideoTitle(videoUri);
            }
        }
    }

    private void promptForVideoTitle(Uri videoUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Video Title");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String videoTitle = input.getText().toString().trim();
            if (!videoTitle.isEmpty()) {
                Video newVideo = new Video(videoTitle, videoUri.toString());
                videoList.add(newVideo);
                videoAdapter.notifyDataSetChanged();
                videoCount++;
                Toast.makeText(this, "Video added to the list", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Video title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void uploadAllVideos() {
        if (videoList.isEmpty()) {
            Toast.makeText(this, "No videos to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Uploading all videos...");
        progressDialog.show();

        int[] index = {0};
        uploadNextVideo(index);
    }

    private void uploadNextVideo(int[] index) {
        if (index[0] >= videoList.size()) {
            progressDialog.dismiss();
            Toast.makeText(this, "All videos uploaded successfully!", Toast.LENGTH_SHORT).show();

            // Clear the list and reset video count
            videoList.clear();
            videoCount = 0;
            videoAdapter.notifyDataSetChanged();

            Intent intent = new Intent(UploadVideosActivity.this, TeacherDashboard.class);
            startActivity(intent);
            finish();
            return;
        }

        Video video = videoList.get(index[0]);
        Uri videoUri = Uri.parse(video.getUrl());
        String videoTitle = video.getTitle();
        String videoId = UUID.randomUUID().toString();
        StorageReference videoRef = storageReference.child(videoId + ".mp4");

        videoRef.putFile(videoUri)
                .addOnSuccessListener(taskSnapshot -> videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String videoUrl = uri.toString();
                    saveVideoToDatabase(videoId, videoTitle, videoUrl, () -> {
                        index[0]++;
                        uploadNextVideo(index);
                    });
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveVideoToDatabase(String videoId, String videoTitle, String videoUrl, Runnable onSuccessCallback) {
        HashMap<String, Object> videoData = new HashMap<>();
        videoData.put("title", videoTitle);
        videoData.put("url", videoUrl);

        databaseReference.child(courseId).child("videos").child(videoId).setValue(videoData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Uploaded: " + videoTitle, Toast.LENGTH_SHORT).show();
                        onSuccessCallback.run();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to save video: " + videoTitle, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
