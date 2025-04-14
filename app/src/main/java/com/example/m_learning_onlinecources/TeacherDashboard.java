package com.example.m_learning_onlinecources;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TeacherDashboard extends AppCompatActivity {
    Button btnAddCourse, btnEditCourses, btnUpdateProfile,btnViewRoutine;
    TextView teacherGreeting;
    Button btnGiveAttendance;
    Button btnAddMaterials;
    Button btnAddNotice, btnViewNotice, btnAddStudents;
    private static final int PICK_FILE_REQUEST_CODE = 1;
    private Button btnUploadRoutine;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher_dashboard);

        btnAddCourse = findViewById(R.id.btn_add_course);
        btnEditCourses = findViewById(R.id.btn_edit_courses);
        btnUpdateProfile = findViewById(R.id.btn_update_profile);
        btnGiveAttendance = findViewById(R.id.btn_give_attendance);
        btnAddNotice = findViewById(R.id.btn_add_notice);
        btnViewNotice = findViewById(R.id.btn_views_notices);
        btnUploadRoutine = findViewById(R.id.btn_upload_routine);
        btnViewRoutine = findViewById(R.id.btn_view_routine);
        teacherGreeting = findViewById(R.id.teacherGreeting);
        btnAddMaterials = findViewById(R.id.btn_add_materials);
        btnAddStudents = findViewById(R.id.btn_add_student);

        // Get the current teacher's ID from Firebase Authentication
        String teacherId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Fetch teacher's name and gender from Firestore
        fetchTeacherDetails(teacherId);


        btnAddCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, AddCourseActivity.class);
                startActivity(intent);
            }
        });

        btnEditCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, EditCoursesActivity.class);
                startActivity(intent);
            }
        });

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, UpdateProfileTeacher.class);
                startActivity(intent);
            }
        });

        btnGiveAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, AttendanceActivity.class);
                startActivity(intent);
            }
        });

        btnAddNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, AddNoticeActivity.class);
                startActivity(intent);
            }
        });

        btnViewNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, ViewNoticesActivity.class);
                startActivity(intent);
            }
        });

        btnUploadRoutine.setOnClickListener(view -> openFilePicker());

        btnViewRoutine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, ViewRoutineActivity.class);
                startActivity(intent);
            }
        });

        btnAddMaterials.setOnClickListener(v -> {
            Intent intent = new Intent(TeacherDashboard.this, AddMaterialsActivity.class);
            startActivity(intent);
        });

        btnAddStudents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboard.this, AdminAddStudent.class);
                startActivity(intent);
            }
        });
    }

    // To display the Teacher Name in Welcome Message
    private void fetchTeacherDetails(String teacherId) {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("teachers")
                .document(teacherId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String gender = documentSnapshot.getString("gender");

                            // Determine greeting prefix based on gender
                            String greetingPrefix = (gender != null && gender.equalsIgnoreCase("female")) ? "Mrs." : "Mr.";
                            String greetingText = "Welcome " + greetingPrefix + " " + name + " to the Teacher Dashboard";

                            // Update the greeting TextView
                            teacherGreeting.setText(greetingText);
                        } else {
                            teacherGreeting.setText("Welcome to the Teacher Dashboard");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TeacherDashboard", "Error fetching teacher data", e);
                        teacherGreeting.setText("Welcome to the Teacher Dashboard");
                    }
                });
    }

    // Function to open file picker
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // You can specify a type like "application/pdf" for PDFs only
        startActivityForResult(Intent.createChooser(intent, "Select Routine File"), PICK_FILE_REQUEST_CODE);
    }

    // Handling the result of file picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                uploadFileToFirebaseStorage(fileUri);
            }
        }
    }

    // Function to upload the selected Routine file to Firebase Storage
    private void uploadFileToFirebaseStorage(Uri fileUri) {
        // Show a dialog to get the title from the user
        showTitleInputDialog(fileUri);
    }

    // Function to show a dialog for title input
    private void showTitleInputDialog(Uri fileUri) {
        // Create an AlertDialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Routine Title");

        // Create an EditText for user input
        final EditText input = new EditText(this);
        input.setHint("Routine Title");
        builder.setView(input);

        // Set "OK" and "Cancel" buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            String title = input.getText().toString().trim();

            if (!title.isEmpty()) {
                // Continue with the upload
                proceedWithFileUpload(fileUri, title);
            } else {
                // Notify user about the empty title
                Toast.makeText(TeacherDashboard.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Dismiss the dialog and cancel the upload
            dialog.cancel();
            Toast.makeText(TeacherDashboard.this, "Routine upload canceled", Toast.LENGTH_SHORT).show();
        });

        // Show the dialog
        builder.show();
    }

    // Proceed with the file upload
    private void proceedWithFileUpload(Uri fileUri, String title) {
        // Initialize and show the progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Routine...");
        progressDialog.setMessage("Please wait while your routine is being uploaded.");
        progressDialog.setCancelable(false); // Prevent closing while uploading
        progressDialog.show();

        // Get the MIME type of the file
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(fileUri);

        // Create Firebase Storage reference for uploading the file
        StorageReference storageRef = FirebaseStorage.getInstance().getReference()
                .child("routines/" + System.currentTimeMillis());

        UploadTask uploadTask = storageRef.putFile(fileUri);

        // Listen for upload progress and completion
        uploadTask.addOnProgressListener(taskSnapshot -> {
            // Calculate progress percentage
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            // Update the progress dialog message with current percentage
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        }).addOnSuccessListener(taskSnapshot -> {
            // On successful upload, retrieve download URL
            storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Save download URL to Firestore with the title
                saveRoutineToFirestore(downloadUri.toString(), mimeType, title);
                // Dismiss progress dialog and show success message
                progressDialog.dismiss();
                Toast.makeText(TeacherDashboard.this, "Routine uploaded successfully", Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            // If upload fails, dismiss progress dialog and show error message
            progressDialog.dismiss();
            Toast.makeText(TeacherDashboard.this, "Failed to upload routine", Toast.LENGTH_SHORT).show();
        });
    }

    // Updated saveRoutineToFirestore function
    private void saveRoutineToFirestore(String downloadUrl, String mimeType, String title) {
        // Data to save in Firestore
        Map<String, Object> routineData = new HashMap<>();
        routineData.put("fileUrl", downloadUrl);
        routineData.put("fileType", mimeType);
        routineData.put("title", title);
        routineData.put("uploadedAt", Timestamp.now());

        // Firestore instance and collection
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("routines")
                .add(routineData)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(TeacherDashboard.this, "Routine saved to Database", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(TeacherDashboard.this, "Failed to save routine in Database", Toast.LENGTH_SHORT).show()
                );
    }
    @Override
    public void onBackPressed() {
        // Show a confirmation dialog when the back button is pressed
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Logout Confirmation")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Navigate to the LoginActivity
                        Intent intent = new Intent(TeacherDashboard.this, LoginScreen.class);
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

