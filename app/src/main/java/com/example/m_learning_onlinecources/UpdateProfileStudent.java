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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;  // Picasso library to load images from URL

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateProfileStudent extends AppCompatActivity {

    private EditText etName, etDob, etPhone, etAddress, etClass, etSection, etEmail, etPassword, etConfirmPassword;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile_student);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePictures");

        // Initialize Views
        etName = findViewById(R.id.et_name);
        etDob = findViewById(R.id.et_dob);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etClass = findViewById(R.id.et_class);
        etSection = findViewById(R.id.et_section);
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
        String gender = intent.getStringExtra("gender");
        String dob = intent.getStringExtra("dob");
        String phone = intent.getStringExtra("phone");
        String address = intent.getStringExtra("address");
        String classs = intent.getStringExtra("classs");
        String section = intent.getStringExtra("section");
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");

        // Populate the fields
        etName.setText(name);
        etDob.setText(dob);
        etPhone.setText(phone);
        etAddress.setText(address);
        etClass.setText(classs);
        etSection.setText(section);
        etEmail.setText(email);
        etPassword.setText(password);

        // Set the selected gender
        if ("Male".equalsIgnoreCase(gender)) {
            ((RadioButton) findViewById(R.id.rb_male)).setChecked(true);
        } else if ("Female".equalsIgnoreCase(gender)) {
            ((RadioButton) findViewById(R.id.rb_female)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rb_other)).setChecked(true);
        }

        // Fetch current user data from Firestore
        fetchUserData();
        
        // Set a date picker dialog for the Date of Birth field
        etDob.setOnClickListener(v -> showDatePickerDialog());
        // Upload Profile Picture
        btnUploadPicture.setOnClickListener(v -> openImagePicker());

        // Update Profile
        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void showDatePickerDialog() {
        // Get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            // Set the selected date in the EditText
            String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
            etDob.setText(selectedDate);
        }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }

    private void fetchUserData() {
        String studentId = mAuth.getCurrentUser().getUid(); // assuming studentId is the current logged-in user’s UID

        db.collection("students")
                .whereEqualTo("userId", studentId)  // Assuming `userId` field is used to identify student
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        Log.d("StudentDashboard", "Query successful, document count: " + queryDocumentSnapshots.size());

                        // Populate fields with the retrieved data
                        String name = documentSnapshot.getString("name");
                        String gender = documentSnapshot.getString("gender");
                        String dob = documentSnapshot.getString("dob");
                        String phone = documentSnapshot.getString("phone");
                        String address = documentSnapshot.getString("address");
                        String classs = documentSnapshot.getString("classs");
                        String section = documentSnapshot.getString("section");
                        String email = documentSnapshot.getString("email");
                        String password = documentSnapshot.getString("password");
                        String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");  // Profile Picture URL

                        // Populate the fields
                        etName.setText(name);
                        etDob.setText(dob);
                        etPhone.setText(phone);
                        etAddress.setText(address);
                        etClass.setText(classs);
                        etSection.setText(section);
                        etEmail.setText(email);
                        etPassword.setText(password);

                        // Set the selected gender
                        if ("Male".equalsIgnoreCase(gender)) {
                            ((RadioButton) findViewById(R.id.rb_male)).setChecked(true);
                        } else if ("Female".equalsIgnoreCase(gender)) {
                            ((RadioButton) findViewById(R.id.rb_female)).setChecked(true);
                        } else {
                            ((RadioButton) findViewById(R.id.rb_other)).setChecked(true);
                        }

                        // Load the profile picture if URL exists
                        if (profilePictureUrl != null && !profilePictureUrl.isEmpty()) {
                            Picasso.get().load(profilePictureUrl).into(ivProfilePicture); // Use Picasso to load image
                        }
                    } else {
                        Log.e("Firestore Data", "No document found for userId: " + studentId);
                        Toast.makeText(this, "No profile data found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore Error", "Error fetching document: ", e);
                    Toast.makeText(this, "Failed to fetch profile data.", Toast.LENGTH_SHORT).show();
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
        String dob = etDob.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String studentClass = etClass.getText().toString().trim();
        String section = etSection.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId != -1) {
            rbSelectedGender = findViewById(selectedGenderId);
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(address) || TextUtils.isEmpty(studentClass) ||
                TextUtils.isEmpty(section) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the profile picture is not updated
        if (uploadedImageUrl == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Update Profile")
                    .setMessage("You haven't selected a profile picture. Do you want to update the profile without updating the picture?")
                    .setPositiveButton("Yes", (dialog, which) -> updateProfileData(false))
                    .setNegativeButton("No", null)
                    .show();
        } else {
            // Proceed with updating profile including the picture
            updateProfileData(true);
        }
    }

    private void updateProfileData(boolean isPictureUpdated) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Prepare data to update
        Map<String, Object> updatedStudentData = new HashMap<>();
        updatedStudentData.put("name", etName.getText().toString().trim());
        updatedStudentData.put("dob", etDob.getText().toString().trim());
        updatedStudentData.put("phone", etPhone.getText().toString().trim());
        updatedStudentData.put("address", etAddress.getText().toString().trim());
        updatedStudentData.put("class", etClass.getText().toString().trim());
        updatedStudentData.put("section", etSection.getText().toString().trim());
        updatedStudentData.put("email", etEmail.getText().toString().trim());
        updatedStudentData.put("password", etConfirmPassword.getText().toString().trim());
        updatedStudentData.put("gender", rbSelectedGender != null ? rbSelectedGender.getText().toString() : "Other");

        // Add the profile picture URL if it's updated
        if (isPictureUpdated) {
            updatedStudentData.put("profilePictureUrl", uploadedImageUrl);
        }

        // Get the current user's ID
        String userId = mAuth.getCurrentUser().getUid();

        // Find the correct document ID for this user
        db.collection("students")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update the document with the correct ID
                        db.collection("students").document(documentId)
                                .set(updatedStudentData, SetOptions.merge())
                                .addOnSuccessListener(aVoid -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();

                                    // Navigate back to the Student Dashboard
                                    Intent intent = new Intent(UpdateProfileStudent.this, StudentDashboard.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish(); // Close the current activity
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(this, "Profile update failed.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                });
    }

}
