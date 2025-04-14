package com.example.m_learning_onlinecources;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfileTeacher extends AppCompatActivity {

    private EditText etName,etDesignation,etClass, etSection, etDob, etPhone, etAddress, etEmail, etPassword, etConfirmPassword, etSubject;
    private RadioGroup rgGender;
    private RadioButton rbSelectedGender;
    private Button btnUploadPicture, btnUpdate;
    private ImageView ivProfilePicture;

    private Uri selectedImageUri;
    private String uploadedImageUrl;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;
    private RadioButton rbMale, rbFemale, rbOther;
    private AlertDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_teacher);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("TeacherProfilePictures");

        // Initialize Views
        etName = findViewById(R.id.et_name);
        etDesignation = findViewById(R.id.et_designation);
        etClass = findViewById(R.id.et_class);
        etSection = findViewById(R.id.et_section);
        etDob = findViewById(R.id.et_dob);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        rbOther = findViewById(R.id.rb_other);
        btnUploadPicture = findViewById(R.id.btn_upload_picture);
        btnUpdate = findViewById(R.id.btn_update);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);

        // Retrieve data passed from the previous activity
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String designation = intent.getStringExtra("designation");
        String classs = intent.getStringExtra("class");
        String section = intent.getStringExtra("name");
        String gender = intent.getStringExtra("section");
        String dob = intent.getStringExtra("dob");
        String phone = intent.getStringExtra("phone");
        String address = intent.getStringExtra("address");
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        // Populate the fields
        etName.setText(name);
        etDesignation.setText(designation);
        etClass.setText(classs);
        etSection.setText(section);
        etDob.setText(dob);
        etPhone.setText(phone);
        etAddress.setText(address);
        etEmail.setText(email);
        etPassword.setText(password);

        // Set the selected gender
        if ("Male".equalsIgnoreCase(gender)) {
            rbMale.setChecked(true);
        } else if ("Female".equalsIgnoreCase(gender)) {
            rbFemale.setChecked(true);
        } else {
            rbOther.setChecked(true);
        }

        // Fetch current user data from Firestore
        fetchUserData();

        etDob.setOnClickListener(v -> showDatePickerDialog());

        // Upload Profile Picture
        btnUploadPicture.setOnClickListener(v -> openImagePicker());

        // Update Profile
        btnUpdate.setOnClickListener(v -> updateProfile());

    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
            etDob.setText(formattedDate);
        }, year, month, day);

        datePickerDialog.show();
    }
    private void fetchUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressDialog("Loading profile...");

        // Fetch data from Firestore
        db.collection("teachers").document(user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    dismissProgressDialog();
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();

                        // Set values to input fields
                        etName.setText(document.getString("name"));
                        etDesignation.setText(document.getString("designation"));
                        etClass.setText(document.getString("class"));
                        etSection.setText(document.getString("section"));
                        etDob.setText(document.getString("dob"));
                        etPhone.setText(document.getString("phone"));
                        etAddress.setText(document.getString("address"));
                        etEmail.setText(document.getString("email"));

                        // Set gender radio button
                        String gender = document.getString("gender");
                        if ("Male".equalsIgnoreCase(gender)) {
                            rbMale.setChecked(true);
                        } else if ("Female".equalsIgnoreCase(gender)) {
                            rbFemale.setChecked(true);
                        } else {
                            rbOther.setChecked(true);
                        }

                        // Load profile picture if available
                        String profilePictureUrl = document.getString("profile_picture_url");
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Picasso.get().load(profilePictureUrl).into(ivProfilePicture);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    dismissProgressDialog();
                    Toast.makeText(this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                ivProfilePicture.setImageBitmap(bitmap);
                ivProfilePicture.setVisibility(View.VISIBLE);
                uploadImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading picture...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            StorageReference fileRef = storageReference.child(mAuth.getCurrentUser().getUid() + ".jpg");
            fileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                uploadedImageUrl = uri.toString();
                                progressDialog.dismiss();
                                Toast.makeText(this, "Picture uploaded successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressDialog.dismiss();
                                Toast.makeText(this, "Failed to get picture URL.", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Picture upload failed.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String designation = etDesignation.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            rbSelectedGender = findViewById(selectedGenderId);
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(address) || TextUtils.isEmpty(designation) ||
                TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uploadedImageUrl == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Update Profile")
                    .setMessage("You haven't selected a profile picture. Do you want to update the profile without updating the picture?")
                    .setPositiveButton("Yes", (dialog, which) -> updateProfileData(false))
                    .setNegativeButton("No", null)
                    .show();
        } else {
            updateProfileData(true);
        }
    }

    private void updateProfileData(boolean isPictureUpdated) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Prepare data to update
        Map<String, Object> updatedTeacherData = new HashMap<>();
        updatedTeacherData.put("name", etName.getText().toString().trim());
        updatedTeacherData.put("designation", etDesignation.getText().toString().trim());
        updatedTeacherData.put("dob", etDob.getText().toString().trim());
        updatedTeacherData.put("phone", etPhone.getText().toString().trim());
        updatedTeacherData.put("password", etConfirmPassword.getText().toString().trim());
        updatedTeacherData.put("address", etAddress.getText().toString().trim());
        updatedTeacherData.put("email", etEmail.getText().toString().trim());
        updatedTeacherData.put("gender", rbSelectedGender != null ? rbSelectedGender.getText().toString() : "Other");

        // Add the profile picture URL if it's updated
        if (isPictureUpdated) {
            updatedTeacherData.put("profile_picture_url", uploadedImageUrl);
        }

        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firestore data directly using the UID as document ID
        db.collection("teachers").document(user.getUid())
                .set(updatedTeacherData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // If password fields are not empty, update password
                    String password = etPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    if (!TextUtils.isEmpty(password) && password.equals(confirmPassword)) {
                        updatePassword(password, progressDialog);
                    } else if (!TextUtils.isEmpty(password)) {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                        navigateToDashboard();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UpdateProfile", "Update failed", e);
                });
    }
    private void updatePassword(String newPassword, ProgressDialog progressDialog) {
        mAuth.getCurrentUser().updatePassword(newPassword)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile and password updated successfully.", Toast.LENGTH_SHORT).show();
                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Profile updated, but password update failed.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showProgressDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(R.layout.progress_dialog);
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private void navigateToDashboard() {
        Intent intent = new Intent(UpdateProfileTeacher.this, TeacherDashboard.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
