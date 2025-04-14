package com.example.m_learning_onlinecources;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class JoinCourseActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CoursesAdapter coursesAdapter;
    private DatabaseReference databaseReference;
    private List<Course> courseList;
    private String currentStudentName;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_course);

        recyclerView = findViewById(R.id.recycler_courses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseReference = FirebaseDatabase.getInstance().getReference("courses");
        courseList = new ArrayList<>();
        coursesAdapter = new CoursesAdapter(courseList, this::joinCourse, FirebaseAuth.getInstance().getCurrentUser().getUid());
        recyclerView.setAdapter(coursesAdapter);
        db = FirebaseFirestore.getInstance();

        String uniqueStudentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchStudentName(uniqueStudentId);

        fetchCourses();
    }

    private void fetchCourses() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    Course course = courseSnapshot.getValue(Course.class);
                    if (course != null) {
                        int videoCount = (int) courseSnapshot.child("videos").getChildrenCount(); // Calculate video count dynamically
                        course.setVideoCount(videoCount);
                        courseList.add(course);
                    }
                }
                coursesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(JoinCourseActivity.this, "Error fetching courses: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void joinCourse(Course course) {
        if (currentStudentName == null || currentStudentName.isEmpty()) {
            Toast.makeText(this, "Student name is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("enrollments").child(currentStudentName);

        studentRef.child("courses").child(course.getId()).setValue(course)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "You are now pursuing the course: " + course.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error joining course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void fetchStudentName(String uniqueStudentId) {
        db.collection("students")
                .whereEqualTo("userId", uniqueStudentId) // Query by userId field
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        currentStudentName = queryDocumentSnapshots.getDocuments().get(0).getString("name");
                        if (currentStudentName == null || currentStudentName.isEmpty()) {
                            Toast.makeText(this, "Student name not found in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No student data found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error fetching student data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
