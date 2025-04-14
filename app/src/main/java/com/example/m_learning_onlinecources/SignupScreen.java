package com.example.m_learning_onlinecources;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SignupScreen extends AppCompatActivity {

    private EditText etName, etPhone, etDob, etEmail,etPassword, etConfirmPassword,
            etClass, etSection,etRoll;
    private RadioGroup rgGender;
    private RadioButton rbSelectedGender;
    private Button btnUploadPicture, btnSignUp;
    private ImageView ivProfilePicture;
    private Uri selectedImageUri;
    private CheckBox cbTerms;
    private AlertDialog progressDialog;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    private static final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_screen);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etDob = findViewById(R.id.et_dob);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        rgGender = findViewById(R.id.rg_gender);
        etClass = findViewById(R.id.et_class);
        etSection = findViewById(R.id.et_section);
        etRoll = findViewById(R.id.et_roll);
        btnUploadPicture = findViewById(R.id.btn_upload_picture);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        cbTerms = findViewById(R.id.cb_terms);
        btnSignUp = findViewById(R.id.btn_sign_up);

        btnUploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivProfilePicture.setImageURI(selectedImageUri);
            ivProfilePicture.setVisibility(View.VISIBLE);
        }
    }

    private void registerUser() {
        String name = etName.getText().toString();
        String phone = etPhone.getText().toString();
        String dob = etDob.getText().toString();
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String className = etClass.getText().toString();
        String section = etSection.getText().toString();
        String roll = etRoll.getText().toString();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        rbSelectedGender = findViewById(selectedGenderId);
        String gender = rbSelectedGender != null ? rbSelectedGender.getText().toString() : "";

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(email)
                || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(gender)
                || TextUtils.isEmpty(className) || TextUtils.isEmpty(section)) { // Include Class and Section
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept terms and conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog("Please check your email and verify it...");
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(verificationTask -> {
                                if (verificationTask.isSuccessful()) {
                                    Toast.makeText(SignupScreen.this, "Verification email sent. Please check your email.", Toast.LENGTH_LONG).show();
                                    checkEmailVerification(user, name, phone, dob, email, gender, password, className, section, roll);
                                } else {
                                    dismissProgressDialog();
                                    Toast.makeText(SignupScreen.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        dismissProgressDialog();
                        Toast.makeText(SignupScreen.this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    dismissProgressDialog();
                    Toast.makeText(SignupScreen.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkEmailVerification(FirebaseUser user, String name, String phone, String dob, String email, String gender, String password, String className, String section, String roll) {
        new Thread(() -> {
            try {
                while (!user.isEmailVerified()) {
                    Thread.sleep(3000);
                    user.reload();
                }

                runOnUiThread(() -> {
                    showProgressDialog("Registering your account...");
                    saveUserToFirestore(name, phone, dob, email, gender, password, className, section, roll);
                    Toast.makeText(SignupScreen.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void saveUserToFirestore(final String name, final String phone,
                                     final String dob, final String email,
                                     final String gender, final String password,
                                     final String className, final String section, final String roll) {
        final Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("phone", phone);
        user.put("dob", dob);
        user.put("email", email);
        user.put("gender", gender);
        user.put("password", password);
        user.put("classs", className);
        user.put("section", section);
        user.put("roll", roll);

        if (selectedImageUri != null) {
            final StorageReference ref = storageReference.child("student_pictures/" + System.currentTimeMillis());
            ref.putFile(selectedImageUri)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ref.getDownloadUrl().addOnCompleteListener(urlTask -> {
                                if (urlTask.isSuccessful()) {
                                    Uri downloadUri = urlTask.getResult();
                                    user.put("profile_picture_url", downloadUri.toString());
                                    saveUserDataToFirestore(user);
                                } else {
                                    dismissProgressDialog();
                                    Toast.makeText(SignupScreen.this, "Failed to get profile picture URL", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            dismissProgressDialog();
                            Toast.makeText(SignupScreen.this, "Profile picture upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        dismissProgressDialog();
                        Toast.makeText(SignupScreen.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveUserDataToFirestore(user);
        }
    }

    private void saveUserDataToFirestore(Map<String, Object> user) {
        user.put("userId", auth.getCurrentUser().getUid());

        firestore.collection("students").add(user)
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();
                    if (task.isSuccessful()) {
                        Toast.makeText(SignupScreen.this, "User data saved successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(SignupScreen.this, "User data save failed", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    dismissProgressDialog();
                    Toast.makeText(SignupScreen.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(message);
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
