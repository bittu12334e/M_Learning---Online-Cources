package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginScreen extends AppCompatActivity {

    private Spinner roleSpinner;
    private EditText etEmailid, etPassword;
    private TextView tvSignup;
    private Button btnLogin ;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private long backPressedTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Initialize views
        roleSpinner = findViewById(R.id.role_spinner);
        etEmailid = findViewById(R.id.et_EmailId);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.sign_up);

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LoginScreen", "Sign Up TextView clicked");
                Intent intent = new Intent(LoginScreen.this, SignupScreen.class);
                startActivity(intent);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String userId = etEmailid.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String selectedRole = roleSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both Email and Password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(userId, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            checkUserRole(selectedRole);
                        } else {
                            handleLoginError(task.getException());
                        }
                    }
                });
    }

    private void handleLoginError(Exception exception) {
        String errorMessage;

        if (exception != null) {
            String errorCode = exception.getMessage();
            if (errorCode.contains("password is invalid") || errorCode.contains("There is no user")) {
                errorMessage = "Invalid email or password. Please try again.";
            } else if (errorCode.contains("badly formatted")) {
                errorMessage = "Invalid email format. Please check and try again.";
            } else if (errorCode.contains("network error")) {
                errorMessage = "Network error. Please check your internet connection.";
            } else {
                errorMessage = "Login failed. Please try again.";
            }
        } else {
            errorMessage = "An unknown error occurred.";
        }

        Toast.makeText(LoginScreen.this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void checkUserRole(String role) {
        // Determine the Firestore collection based on selected role
        String collection = role.equals("Teacher") ? "teachers" :
                role.equals("Student") ? "students" : "admins";

        firestore.collection(collection)
                .whereEqualTo("email", auth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            navigateToDashboard(role);
                        } else {
                            Toast.makeText(LoginScreen.this, "No user record found for this role", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "Teacher":
                intent = new Intent(LoginScreen.this, TeacherDashboard.class);
                break;
            case "Student":
                intent = new Intent(LoginScreen.this, StudentDashboard.class);
                break;
            case "Admin":
                intent = new Intent(LoginScreen.this, AdminDashboard.class);
                break;
            default:
                Toast.makeText(this, "Invalid role selected", Toast.LENGTH_SHORT).show();
                return;
        }
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();  // Exit the app
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            backPressedTime = System.currentTimeMillis();  // Update the backPressedTime to current time
        }
    }

}
