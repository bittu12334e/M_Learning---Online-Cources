package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;




import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceActivity extends AppCompatActivity {

    private RecyclerView recyclerStudents;
    private FirebaseFirestore db;
    private List<Student> studentList;
    private AttendanceAdapter attendanceAdapter;
    private Button btnSubmitAttendance;
    private String teacherClass;
    private String teacherSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        recyclerStudents = findViewById(R.id.rv_students);
        recyclerStudents.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();

        studentList = new ArrayList<>();
        attendanceAdapter = new AttendanceAdapter(studentList);
        recyclerStudents.setAdapter(attendanceAdapter);

        // Initialize the Submit Attendance button
        btnSubmitAttendance = findViewById(R.id.btn_submit_attendance);
        btnSubmitAttendance.setOnClickListener(v -> submitAttendance());

        // Fetch teacher's assigned class and section, then fetch students
        fetchTeacherInfoAndLoadStudents();

    }

    // Method to submit attendance to Firebase
    private void submitAttendance() {
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference("attendance");
        String attendanceId = attendanceRef.push().getKey();

        // Generate the current date in a readable format
        String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a map for attendance data with date and nested student details
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("date", currentDate);  // Add the date of submission

        // Nested map for student attendance details
        Map<String, Object> studentsAttendance = new HashMap<>();
        for (Student student : studentList) {
            String name = student.getName();

            if (name != null && !name.isEmpty()) {
                Map<String, Object> studentData = new HashMap<>();
                //studentData.put("name", name);
                studentData.put("class", student.getClasss());
                studentData.put("section", student.getSection());
                studentData.put("attendance", student.isAttendance());

                studentsAttendance.put(name, studentData);
            } else {
                Log.e("AttendanceActivity", "Missing name for student.");
            }
        }

        // Store attendance data under the unique attendance ID
        if (attendanceId != null) {
            attendanceData.put("students", studentsAttendance);  // Add students data to attendanceData

            attendanceRef.child(attendanceId).setValue(attendanceData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AttendanceActivity.this, "Attendance submitted!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard(); // Navigate to Dashboard after successful submission
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AttendanceActivity.this, "Failed to submit attendance.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Method to navigate back to Dashboard after submission
    private void navigateToDashboard() {
        Intent intent = new Intent(AttendanceActivity.this, TeacherDashboard.class); // Replace with your Dashboard activity class
        startActivity(intent);
        finish();
    }

    private void fetchTeacherInfoAndLoadStudents() {
        String teacherId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("teachers").document(teacherId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        teacherClass = documentSnapshot.getString("class");
                        teacherSection = documentSnapshot.getString("section");
                        fetchStudentData(); // Now fetch student data with the teacher's class and section
                    } else {
                        Toast.makeText(this, "Failed to load teacher info", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch teacher info", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchStudentData() {
        db.collection("students")
                .whereEqualTo("classs", teacherClass)
                .whereEqualTo("section", teacherSection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        studentList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Student student = document.toObject(Student.class);
                            if (student != null && student.getUserId() != null) {
                                studentList.add(student);
                            }
                        }
                        attendanceAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Loaded " + studentList.size() + " students", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to fetch students", Toast.LENGTH_SHORT).show();
                    }
                });
}

}