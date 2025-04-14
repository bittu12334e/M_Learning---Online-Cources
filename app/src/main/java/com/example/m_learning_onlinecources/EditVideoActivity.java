package com.example.m_learning_onlinecources;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EditVideoActivity extends AppCompatActivity {

    private static final int PICK_VIDEO_REQUEST = 1;

    private ListView listViewVideos;
    private Button btnAddNewVideo, btnSaveVideoChanges;
    private ProgressDialog progressDialog;

    private DatabaseReference courseRef;
    private StorageReference storageReference;
    private String courseId;

    private final List<String> videoTitles = new ArrayList<>();
    private final List<String> videoIds = new ArrayList<>();
    private final Map<String, String> videoUrls = new HashMap<>();

    private final List<VideoItem> newVideoList = new ArrayList<>();
    private final Map<String, Uri> newVideos = new HashMap<>();
    private final Map<String, String> updatedTitles = new HashMap<>();

    private String selectedVideoId;
    private String selectedVideoTitle;

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        initializeViews();
        initializeFirebase();
        loadCourseVideos();

        btnAddNewVideo.setOnClickListener(v -> showAddNewVideoDialog());

        btnSaveVideoChanges.setOnClickListener(v -> saveAllChanges());

        listViewVideos.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < videoIds.size()) {
                selectedVideoId = videoIds.get(position);
                selectedVideoTitle = videoTitles.get(position);
                showVideoOptionsDialog();
            } else {
                Toast.makeText(this, "Invalid selection. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews() {
        listViewVideos = findViewById(R.id.listViewVideos);
        btnAddNewVideo = findViewById(R.id.btnAddNewVideo);
        btnSaveVideoChanges = findViewById(R.id.btnSaveVideoChanges);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setCancelable(false);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, videoTitles);
        listViewVideos.setAdapter(adapter);
    }

    private void initializeFirebase() {
        courseId = getIntent().getStringExtra("courseId");
        courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseId).child("videos");
        storageReference = FirebaseStorage.getInstance().getReference("course_videos");
    }

    private void loadCourseVideos() {
        progressDialog.setMessage("Fetching videos...");
        progressDialog.show();

        courseRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.dismiss();
                videoTitles.clear();
                videoIds.clear();
                videoUrls.clear();

                for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                    String videoTitle = videoSnapshot.child("title").getValue(String.class);
                    String videoId = videoSnapshot.getKey();
                    String videoUrl = videoSnapshot.child("url").getValue(String.class);

                    if (videoTitle != null && videoId != null && videoUrl != null) {
                        videoTitles.add(videoTitle);
                        videoIds.add(videoId);
                        videoUrls.put(videoId, videoUrl);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(EditVideoActivity.this, "Failed to load videos: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVideoOptionsDialog() {
        String[] options = {"Edit Title", "Replace Video", "Review Video"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Video Options");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Edit Title
                    showEditTitleDialog();
                    break;
                case 1: // Replace Video
                    selectVideoToReplace();
                    break;
                case 2: // Review Video
                    reviewVideo();
                    break;
            }
        });
        builder.show();
    }

    private void reviewVideo() {
        String videoUrl = newVideos.containsKey(selectedVideoId)
                ? newVideos.get(selectedVideoId).toString()
                : videoUrls.get(selectedVideoId);

        if (videoUrl != null) {
            Intent intent = new Intent(this, VideoPlayerActivity.class);
            intent.putExtra("videoUrl", videoUrl);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Video URL not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectVideoToReplace() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    private void showEditTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Video Title");

        final EditText input = new EditText(this);
        input.setText(selectedVideoTitle);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                updatedTitles.put(selectedVideoId, newTitle);
                videoTitles.set(videoIds.indexOf(selectedVideoId), newTitle);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Title updated locally.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddNewVideoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Video");

        final EditText input = new EditText(this);
        input.setHint("Enter video title");
        builder.setView(input);

        builder.setPositiveButton("Next", (dialog, which) -> {
            String videoTitle = input.getText().toString().trim();
            if (!videoTitle.isEmpty()) {
                VideoItem newVideo = new VideoItem(videoTitle, null);
                String newVideoId = UUID.randomUUID().toString();
                newVideo.setVideoId(newVideoId);
                newVideoList.add(newVideo);
                videoTitles.add(videoTitle);
                videoIds.add(newVideoId);
                adapter.notifyDataSetChanged();
                selectVideo(videoTitle, newVideoId);
            } else {
                Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void selectVideo(String videoTitle, String newVideoId) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedVideoUri = data.getData();
            if (selectedVideoId != null) {
                newVideos.put(selectedVideoId, selectedVideoUri);
                Toast.makeText(this, "Video selected. Will replace after saving changes.", Toast.LENGTH_SHORT).show();
            } else if (!newVideoList.isEmpty()) {
                VideoItem lastAddedVideo = newVideoList.get(newVideoList.size() - 1);
                lastAddedVideo.setVideoUri(selectedVideoUri);
                Toast.makeText(this, "New video added.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void saveAllChanges() {
        progressDialog.setMessage("Saving all changes...");
        progressDialog.show();

        Map<String, Object> updates = new HashMap<>();

        // Update titles directly
        for (Map.Entry<String, String> entry : updatedTitles.entrySet()) {
            String videoId = entry.getKey();
            String newTitle = entry.getValue();
            updates.put(videoId + "/title", newTitle);
        }

        // Use a counter to track upload completion
        int totalUploads = newVideos.size() + newVideoList.size();
        final int[] completedUploads = {0};

        // Helper function to track progress
        Runnable checkCompletion = () -> {
            completedUploads[0]++;
            if (completedUploads[0] == totalUploads) {
                courseRef.updateChildren(updates).addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "All changes saved successfully.", Toast.LENGTH_SHORT).show();
                        newVideoList.clear();
                        updatedTitles.clear();
                        newVideos.clear();
                        loadCourseVideos();
                        navigateToTeacherDashboard();
                    } else {
                        Toast.makeText(this, "Failed to save changes: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        // Replace videos
        for (Map.Entry<String, Uri> entry : newVideos.entrySet()) {
            String videoId = entry.getKey();
            Uri videoUri = entry.getValue();
            String oldVideoUrl = videoUrls.get(videoId);

            if (oldVideoUrl != null) {
                // Delete old video
                StorageReference oldVideoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldVideoUrl);
                oldVideoRef.delete();
            }

            String newVideoFileName = UUID.randomUUID().toString() + ".mp4";
            StorageReference newVideoRef = storageReference.child(newVideoFileName);

            newVideoRef.putFile(videoUri).addOnSuccessListener(taskSnapshot ->
                    newVideoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updates.put(videoId + "/url", uri.toString());
                        checkCompletion.run();
                    })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        // Add new videos
        for (VideoItem videoItem : newVideoList) {
            if (videoItem.getVideoUri() != null) {
                String videoFileName = UUID.randomUUID().toString() + ".mp4";
                StorageReference videoRef = storageReference.child(videoFileName);

                videoRef.putFile(videoItem.getVideoUri()).addOnSuccessListener(taskSnapshot ->
                        videoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String videoId = courseRef.push().getKey();
                            if (videoId != null) {
                                updates.put(videoId + "/title", videoItem.getVideoTitle());
                                updates.put(videoId + "/url", uri.toString());
                            }
                            checkCompletion.run();
                        })).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }

        // If there are no uploads, directly save updates
        if (totalUploads == 0) {
            courseRef.updateChildren(updates).addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(this, "All changes saved successfully.", Toast.LENGTH_SHORT).show();
                    loadCourseVideos();
                    navigateToTeacherDashboard();
                } else {
                    Toast.makeText(this, "Failed to save changes: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToTeacherDashboard() {
        Intent intent = new Intent(EditVideoActivity.this, TeacherDashboard.class);
        startActivity(intent);
        finish();
    }

}
