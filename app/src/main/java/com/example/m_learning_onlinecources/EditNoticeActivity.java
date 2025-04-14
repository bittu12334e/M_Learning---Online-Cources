package com.example.m_learning_onlinecources;

import static com.example.m_learning_onlinecources.AddNoticeActivity.PICK_FILE_REQUEST;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditNoticeActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private TextView tvFileName;
    private Button btnUpdate, btnReplaceFile, btnViewFile;
    private FirebaseFirestore firestore;
    private String noticeId;
    private String fileUrl;  // To store the new file URI
    private String oldFileUrl;  // To store the URL of the old file (to be deleted)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notice);

        // Get the noticeId passed via Intent
        noticeId = getIntent().getStringExtra("noticeId");

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize UI components
        etTitle = findViewById(R.id.et_edit_title);
        etDescription = findViewById(R.id.et_edit_description);
        tvFileName = findViewById(R.id.tv_edit_file_name);
        btnUpdate = findViewById(R.id.btn_update_notice);
        btnReplaceFile = findViewById(R.id.btn_replace_file);

        // Fetch existing notice data from Firestore
        fetchNoticeData();

        // Replace File Button - Pick a new file
        btnReplaceFile.setOnClickListener(v -> openFilePicker());

        // Update Notice Button
        btnUpdate.setOnClickListener(v -> {
            String updatedTitle = etTitle.getText().toString();
            String updatedDescription = etDescription.getText().toString();

            if (fileUrl != null) {  // If a new file is selected
                replaceOldFileAndUpdateNotice(noticeId, updatedTitle, updatedDescription);
            } else {
                // Update the notice without changing the file
                updateNotice(noticeId, updatedTitle, updatedDescription, oldFileUrl);
            }
        });
    }

    // Fetch the existing notice data from Firestore
    private void fetchNoticeData() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        DocumentReference noticeRef = firestore.collection("notices").document(noticeId);
        noticeRef.get().addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String title = document.getString("title");
                    String description = document.getString("description");
                    oldFileUrl = document.getString("fileUrl");

                    // Set the previous title and description
                    etTitle.setText(title);
                    etDescription.setText(description);

                    // Show file details if there is a file attached
                    if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
                        tvFileName.setText(getFileNameFromUrl(oldFileUrl));


                    } else {
                        tvFileName.setText("No file attached");
                        btnViewFile.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(EditNoticeActivity.this, "No such notice", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity if notice does not exist
                }
            } else {
                Toast.makeText(EditNoticeActivity.this, "Failed to load notice", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to open file with suggested apps


    // Helper method to get the file name from URL
    private String getFileNameFromUrl(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
    }


    // Open file picker to select a new file
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                Uri fileUri = data.getData(); // Get the selected file URI
                fileUrl = fileUri.toString(); // Save the URL as a String
                String fileName = getFileName(fileUri);
                tvFileName.setText(fileName); // Show the new file name
            } catch (Exception e) {
                Toast.makeText(this, "Failed to get file URL", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    // Replace the old file, then upload the new file and update the notice
    private void replaceOldFileAndUpdateNotice(String noticeId, String title, String description) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Replacing file...");
        progressDialog.show();

        // Delete the old file if it exists
        if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
            StorageReference oldFileRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldFileUrl);
            oldFileRef.delete().addOnSuccessListener(aVoid -> {
                // After successfully deleting the old file, upload the new one
                uploadNewFile(noticeId, title, description, progressDialog);
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(EditNoticeActivity.this, "Failed to delete old file", Toast.LENGTH_SHORT).show();
            });
        } else {
            // No old file to delete, directly upload the new one
            uploadNewFile(noticeId, title, description, progressDialog);
        }
    }

    // Upload the new file and update the notice
    private void uploadNewFile(String noticeId, String title, String description, ProgressDialog progressDialog) {
        if (fileUrl == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Uri fileUri = Uri.parse(fileUrl); // Convert String URL back to Uri
            StorageReference fileRef = FirebaseStorage.getInstance().getReference("notice_files")
                    .child(System.currentTimeMillis() + "_" + getFileName(fileUri));

            fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    progressDialog.dismiss();
                    fileUrl = uri.toString(); // Update fileUrl with the new file URL
                    updateNotice(noticeId, title, description, fileUrl);
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(EditNoticeActivity.this, "File upload failed", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Invalid file URL", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Update the notice in Firestore
    private void updateNotice(String noticeId, String title, String description, String fileUrl) {
        Map<String, Object> noticeUpdates = new HashMap<>();
        noticeUpdates.put("title", title);
        noticeUpdates.put("description", description);
        noticeUpdates.put("fileUrl", fileUrl);  // Update the file URL

        firestore.collection("notices").document(noticeId)
                .set(noticeUpdates, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditNoticeActivity.this, "Notice updated successfully", Toast.LENGTH_SHORT).show();
                        finish();  // Return to the previous screen
                    } else {
                        Toast.makeText(EditNoticeActivity.this, "Failed to update notice", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to get file name from Uri
    private String getFileName(Uri uri) {
        String path = uri.getPath();
        return path != null ? path.substring(path.lastIndexOf('/') + 1) : "Unknown";
    }
}
