package com.example.m_learning_onlinecources;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.m_learning_onlinecources.AdminDashboard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminAddTeacher extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etDesignation, etPassword;
    private Button btnAddTeacher;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_teacher);

        // Initialize Firebase Authentication and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etDesignation = findViewById(R.id.et_designation);
        etPassword = findViewById(R.id.et_password);
        btnAddTeacher = findViewById(R.id.btn_add_teacher);

        // Handle adding teacher logic
        btnAddTeacher.setOnClickListener(v -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();
            String phone = etPhone.getText().toString();
            String designation = etDesignation.getText().toString();
            String password = etPassword.getText().toString();

            if (!name.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !password.isEmpty()) {
                createTeacherAccount(name, email, phone, designation, password);  // Pass the custom password
            } else {
                Toast.makeText(AdminAddTeacher.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTeacherAccount(String name, String email, String phone, String designation, String password) {
        // Create user with email and password using Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();
                            saveTeacherToDatabase(uid, name, email, phone, designation,password);
                        }
                    } else {
                        Toast.makeText(AdminAddTeacher.this, "Teacher addition failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void saveTeacherToDatabase(String uid, String name, String email, String phone, String designation, String password) {

        Teacher teacher = new Teacher(name, email, phone, designation, email, password);

        db.collection("teachers").document(uid).set(teacher)
                .addOnSuccessListener(aVoid -> {
                    // Show dialog with user ID and password
                    showUserCredentialsDialog(email, password);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminAddTeacher.this, "Error saving teacher data.", Toast.LENGTH_SHORT).show();
                });
    }


    private void showUserCredentialsDialog(String email, String password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Teacher Account Created");

        // Message with Email (User ID) and Password
        builder.setMessage("User ID : " + email + "\nPassword: " + password);

        builder.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent(AdminAddTeacher.this, AdminDashboard.class);
            startActivity(intent);
            finish(); // Optional: finish the current activity
        });

        builder.create().show();
    }
}
