package com.example.m_learning_onlinecources;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class EditRoutineActivity extends AppCompatActivity {
    private static final int PICK_FILE_REQUEST_CODE = 2;
    private Uri newFileUri;
    private String oldFileUrl, routineId, routineTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_routine);

        // Retrieve the current routine details
        Intent intent = getIntent();
        oldFileUrl = intent.getStringExtra("oldFileUrl");
        routineId = intent.getStringExtra("routineId");
        routineTitle = intent.getStringExtra("routineTitle");

        Button btnUploadNewFile = findViewById(R.id.btnUploadNewFile);
        btnUploadNewFile.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            newFileUri = data.getData();
            uploadNewFile();
        }
    }

    private void uploadNewFile() {
        if (newFileUri == null) return;

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading New Routine...");
        progressDialog.show();

        // Upload the new file
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("routines/" + System.currentTimeMillis());
        storageRef.putFile(newFileUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(newUri -> {
                            progressDialog.dismiss();
                            promptForTitleUpdate(newUri.toString());
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to upload new file", Toast.LENGTH_SHORT).show();
                });
    }

    private void promptForTitleUpdate(String newFileUrl) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Routine Title");

        final EditText input = new EditText(this);
        input.setHint("Enter new title");
        builder.setView(input);

        builder.setMessage("Do you want to update the title?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String newTitle = input.getText().toString().trim();
                    if (newTitle.isEmpty()) {
                        Toast.makeText(this, "Title cannot be empty. Using the old title.", Toast.LENGTH_SHORT).show();
                        newTitle = routineTitle;
                    }
                    updateRoutineInFirestore(newFileUrl, newTitle);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    updateRoutineInFirestore(newFileUrl, routineTitle);
                })
                .setCancelable(false);

        builder.show();
    }

    private void updateRoutineInFirestore(String newFileUrl, String title) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("routines").document(routineId)
                .set(new HashMap<String, Object>() {{
                    put("fileUrl", newFileUrl);
                    put("uploadedAt", Timestamp.now());
                    put("title", title);
                }}, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Routine updated", Toast.LENGTH_SHORT).show();
                    deleteOldFileFromStorage();
                    finishWithResult();
                })
                .addOnFailureListener(e -> {
                    Log.e("EditRoutineActivity", "Error setting Firestore document", e);
                    Toast.makeText(this, "Failed to update routine", Toast.LENGTH_SHORT).show();
                });
    }

    private void finishWithResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedRoutineId", routineId); // Optionally pass updated data
        setResult(RESULT_OK, resultIntent);
        finish(); // Close this activity
    }

    private void deleteOldFileFromStorage() {
        StorageReference oldFileRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldFileUrl);
        oldFileRef.delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Old file removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e("EditRoutineActivity", "Error deleting old file", e);
                    Toast.makeText(this, "Failed to delete old file", Toast.LENGTH_SHORT).show();
                });
    }
}
