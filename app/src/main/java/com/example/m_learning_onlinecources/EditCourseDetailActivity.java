package com.example.m_learning_onlinecources;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditCourseDetailActivity extends AppCompatActivity {

    private EditText editTextTitle, editTextDescription,editTextVideoName;
    private Button btnUpdateCourse, btnUpdateVideoActivity;
    private DatabaseReference courseRef;
    private String courseId;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course_detail);

        initializeViews();
        initializeFirebase();

        if (courseId == null || courseId.isEmpty()) {
            Log.e("EditCourseDetail", "Course ID is null or empty.");
            Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if courseId is invalid
            return;
        }

        loadCourseDetails();

        btnUpdateCourse.setOnClickListener(v -> updateCourse());

        btnUpdateVideoActivity.setOnClickListener(v -> {
            Intent intent = new Intent(EditCourseDetailActivity.this, EditVideoActivity.class);
            intent.putExtra("courseId", courseId);
            startActivity(intent);
        });
    }

    private void initializeViews() {
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextVideoName = findViewById(R.id.editTextVideoName);
        editTextDescription = findViewById(R.id.editTextDescription);
        btnUpdateCourse = findViewById(R.id.btnUpdateCourse);
        btnUpdateVideoActivity = findViewById(R.id.btnUpdateVideoActivity);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating Course");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
    }

    private void initializeFirebase() {
        courseId = getIntent().getStringExtra("courseId");
        courseRef = FirebaseDatabase.getInstance().getReference("courses").child(courseId);
    }

    private void loadCourseDetails() {
        progressDialog.show();
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                Course course = dataSnapshot.getValue(Course.class);
                if (course != null) {
                    populateCourseDetails(course);
                } else {
                    Toast.makeText(EditCourseDetailActivity.this, "Course not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressDialog.dismiss();
                Toast.makeText(EditCourseDetailActivity.this, "Failed to load course details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateCourseDetails(Course course) {
        editTextTitle.setText(course.getTitle());
        editTextVideoName.setText(course.getName());
        editTextDescription.setText(course.getDescription());
    }

    private void updateCourse() {
        String title = editTextTitle.getText().toString().trim();
        String name = editTextVideoName.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (title.isEmpty() || name.isEmpty() ||description.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Saving changes...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();

        courseRef.child("title").setValue(title);
        courseRef.child("name").setValue(name);
        courseRef.child("description").setValue(description).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update course", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
