package com.example.m_learning_onlinecources;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddCourseActivity extends AppCompatActivity {

    private EditText courseTitle, courseName, courseDescription;
    private Button saveCourse;
    private TextView textViewVideoSelected;
    private Uri videoUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_course);

        courseTitle = findViewById(R.id.editTextCourseTitle);
        courseName = findViewById(R.id.editTextCourseName);
        courseDescription = findViewById(R.id.editTextCourseDescription);
        saveCourse = findViewById(R.id.buttonSaveCourse);


        databaseReference = FirebaseDatabase.getInstance().getReference("courses");
        storageReference = FirebaseStorage.getInstance().getReference("course_videos");

        progressDialog = new ProgressDialog(this); // Initialize ProgressDialog
        progressDialog.setTitle("Uploading Course");
        progressDialog.setMessage("Uploading in progress...");
        progressDialog.setCancelable(false); // Prevent closing while uploading
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Show percentage


        saveCourse.setOnClickListener(v -> {
            String title = courseTitle.getText().toString().trim();
            String name = courseName.getText().toString().trim();
            String description = courseDescription.getText().toString().trim();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            String uploadedBy = (auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null)
                    ? auth.getCurrentUser().getDisplayName()
                    : "No name set. Update your profile.";

            // Validation
            if (title.isEmpty() || name.isEmpty() || description.isEmpty()) {
                Toast.makeText(AddCourseActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save course data to Firebase
            String courseId = databaseReference.push().getKey(); // Get unique course ID
            Course course = new Course(courseId, title, name, description, uploadedBy);
            databaseReference.child(courseId).setValue(course)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Show confirmation dialog
                            new AlertDialog.Builder(AddCourseActivity.this)
                                    .setTitle("Add Videos")
                                    .setMessage("Course added successfully! Do you want to add videos to this course?")
                                    .setPositiveButton("Yes", (dialog, which) -> {
                                        // Navigate to UploadVideosActivity
                                        Intent intent = new Intent(AddCourseActivity.this, UploadVideosActivity.class);
                                        intent.putExtra("courseId", courseId); // Pass the course ID
                                        intent.putExtra("courseName", courseName.getText().toString().trim());
                                        startActivity(intent);
                                    })
                                    .setNegativeButton("No", (dialog, which) -> {
                                        // Navigate back to teacher dashboard
                                        Toast.makeText(AddCourseActivity.this, "Course saved without videos", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(AddCourseActivity.this, TeacherDashboard.class);
                                        startActivity(intent);
                                        finish(); // Close current activity
                                    })
                                    .show();
                        } else {
                            Toast.makeText(AddCourseActivity.this, "Failed to add course", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            videoUri = data.getData();

            // Show the success message in the TextView
            if (videoUri != null) {
                textViewVideoSelected.setVisibility(TextView.VISIBLE); // Make TextView visible
                textViewVideoSelected.setText("Video selected successfully!"); // Set success message

                // Show a dialog to ask the user if they want to play the video
                new AlertDialog.Builder(AddCourseActivity.this)
                        .setTitle("Preview Video")
                        .setMessage("Do you want to play the video to check it?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Launch VideoPlayerActivity with the selected video URI
                            Intent intent = new Intent(AddCourseActivity.this, VideoPlayerActivity.class);
                            intent.putExtra("videoUri", videoUri.toString());
                            startActivity(intent);
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            // Do nothing
                            dialog.dismiss();
                        })
                        .show();
            }
        }
    }
}
