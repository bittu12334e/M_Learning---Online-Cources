package com.example.m_learning_onlinecources;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAddStudent extends AppCompatActivity {

    private EditText etStudentName, etStudentId, etStudentEmail, etStudentPassword;
    private Button btnAddStudent;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_student);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etStudentName = findViewById(R.id.et_student_name);
        etStudentId = findViewById(R.id.et_student_id);
        etStudentEmail = findViewById(R.id.et_student_email);
        etStudentPassword = findViewById(R.id.et_student_password);
        btnAddStudent = findViewById(R.id.btn_add_student);

        // Handle adding student logic
        btnAddStudent.setOnClickListener(v -> {
            String studentName = etStudentName.getText().toString();
            String studentId = etStudentId.getText().toString();
            String studentEmail = etStudentEmail.getText().toString();
            String studentPassword = etStudentPassword.getText().toString();

            if (!studentName.isEmpty() && !studentId.isEmpty() && !studentEmail.isEmpty() && !studentPassword.isEmpty()) {
                createStudentAccount(studentName, studentId, studentEmail, studentPassword);
            } else {
                Toast.makeText(AdminAddStudent.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createStudentAccount(String name, String studentId, String email, String password) {
        // Create user with email and password using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();
                            saveStudentToDatabase(uid, name, studentId, email, password);
                        }
                    } else {
                        Toast.makeText(AdminAddStudent.this, "Student addition failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveStudentToDatabase(String uid, String name, String studentId, String email, String password) {
        // Create a new Student object with attendance set to false by default
        Student student = new Student(name, studentId, email, password, email, false);

        db.collection("students").document(uid).set(student)
                .addOnSuccessListener(aVoid -> {
                    // Show dialog with user ID and password
                    showUserCredentialsDialog(email, password);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminAddStudent.this, "Error saving student data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showUserCredentialsDialog(String email, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Student Account Created");

        // Message with Email (User ID) and Password
        builder.setMessage("User ID : " + email + "\nPassword: " + password);

        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent(AdminAddStudent.this, AdminDashboard.class);
            startActivity(intent);
            finish(); // Optional: finish the current activity
        });

        builder.create().show();
    }
}
