package com.example.m_learning_onlinecources;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.nullness.qual.NonNull;

public class StudentDashboard extends AppCompatActivity {
    CardView crdJoinCourse,crdViewNotice,crdViewRoutine,crdViewAttendance,
            crdAccessStdRes,crdUpdateProfile;
    TextView studentGreeting;
    String name, gender, dob, phone, address, classs, section, email, password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_dashboard);
        crdJoinCourse = findViewById(R.id.card_join_course);
        crdViewNotice = findViewById(R.id.card_view_notices);
        crdViewRoutine = findViewById(R.id.card_view_routines);
        crdViewAttendance = findViewById(R.id.card_view_attendances);
        crdAccessStdRes = findViewById(R.id.card_access_study_res);
        crdUpdateProfile = findViewById(R.id.card_update_profile);
        studentGreeting = findViewById(R.id.studentGreeting);

        // Get the current student's ID from Firebase Authentication
        String studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch student's name and gender from Firestore
        fetchStudentDetails(studentId);


        crdJoinCourse.setOnClickListener(view -> {
            // Navigate to the Join Course Activity or open a course list
            Intent intent = new Intent(this, JoinCourseActivity.class);
            startActivity(intent);
        });

        crdViewNotice.setOnClickListener(view -> {
            Intent intent = new Intent(this, NoticeViewActivity.class);
            startActivity(intent);
        });

        crdViewRoutine.setOnClickListener(view -> {
            Intent intent = new Intent(this, RoutineViewActivity.class);
            startActivity(intent);
        });

        crdViewAttendance.setOnClickListener(view -> {
            Intent intent = new Intent(this, AttendanceViewActivity.class);
            startActivity(intent);
        });

        crdAccessStdRes.setOnClickListener(view -> {
            Intent intent = new Intent(this, ResourseViewActivity.class);
            startActivity(intent);
        });

        crdUpdateProfile.setOnClickListener(view -> {
            Intent intent = new Intent(this, UpdateProfileStudent.class);
            // Pass student data as extras
            intent.putExtra("name", name);
            intent.putExtra("gender", gender);
            intent.putExtra("dob", dob);
            intent.putExtra("phone", phone);
            intent.putExtra("address", address);
            intent.putExtra("classs", classs);
            intent.putExtra("section", section);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
        });

    }
    private void fetchStudentDetails(String studentId) {
        Log.d("StudentDashboard", "Fetching details for studentId: " + studentId);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("students")
                .whereEqualTo("userId", studentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first matching document
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Log.d("StudentDashboard", "Document Snapshot: " + documentSnapshot.getData());

                        // Retrieve fields
                        name = documentSnapshot.getString("name");
                        gender = documentSnapshot.getString("gender");
                        dob = documentSnapshot.getString("dob");
                        phone = documentSnapshot.getString("phone");
                        address = documentSnapshot.getString("address");
                        classs = documentSnapshot.getString("classs");
                        section = documentSnapshot.getString("section");
                        email = documentSnapshot.getString("email");
                        password = documentSnapshot.getString("password");


                        if (name == null || name.isEmpty()) name = "Student";
                        String greetingPrefix = (gender != null && gender.equalsIgnoreCase("female")) ? "Ms." : "Mr.";
                        String greetingText = "Welcome " + greetingPrefix + " " + name + " to the Student Dashboard";

                        Log.d("StudentDashboard", "Greeting text: " + greetingText);
                        studentGreeting.setText(greetingText);
                    } else {
                        Log.w("StudentDashboard", "No matching document found for userId: " + studentId);
                        studentGreeting.setText("Welcome to the Student Dashboard");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("StudentDashboard", "Error fetching student data: ", e);
                    studentGreeting.setText("Welcome to the Student Dashboard");
                });
    }
    @Override
    public void onBackPressed() {
        // Show a confirmation dialog when the back button is pressed
        new AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Navigate to the LoginActivity
                        Intent intent = new Intent(StudentDashboard.this, LoginScreen.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Close the AdminDashboard activity
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog and stay on the current screen
                    }
                })
                .show();
    }
    }
