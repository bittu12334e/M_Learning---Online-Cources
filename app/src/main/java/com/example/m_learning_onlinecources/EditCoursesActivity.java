package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditCoursesActivity extends AppCompatActivity   {

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private List<Course> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_courses);

        // Initialize RecyclerView and Course List
        recyclerView = findViewById(R.id.recyclerViewCourses);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        courseList = new ArrayList<>();

        // Load courses from Firebase
        loadCoursesFromFirebase();
    }

    private void loadCoursesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("courses");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseList.clear(); // Clear the list to avoid duplicates

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Course course = dataSnapshot.getValue(Course.class);
                    if (course != null) {
                        courseList.add(course); // Add course to the list
                    }
                }

                if (!courseList.isEmpty()) {
                    // Initialize and set the adapter if data exists
                    adapter = new CourseAdapter(courseList, EditCoursesActivity.this);
                    recyclerView.setAdapter(adapter);
                } else {
                    // No courses found
                    Toast.makeText(EditCoursesActivity.this, "No courses found.", Toast.LENGTH_SHORT).show();
                    Log.w("FirebaseData", "No courses available.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log and display Firebase query failure
                Toast.makeText(EditCoursesActivity.this, "Failed to load courses.", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error loading courses", error.toException());
            }
        });
    }

}
