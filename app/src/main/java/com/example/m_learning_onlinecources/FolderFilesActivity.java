package com.example.m_learning_onlinecources;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class FolderFilesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFiles;
    private FileAdapter fileAdapter;
    private List<FileItem> fileList;
    private CollectionReference filesRef;
    private String folderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_files);

        initializeUI();

        folderId = getIntent().getStringExtra("FOLDER_ID");
        if (folderId == null) {
            showErrorMessage("Folder ID is null");
            finish();
            return;
        }

        filesRef = FirebaseFirestore.getInstance().collection("folders_files")
                .document(folderId)
                .collection("files");

        loadFilesFromFirestore();
    }

    private void initializeUI() {
        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        FloatingActionButton fabAddFile = findViewById(R.id.fab_add_file);

        fileList = new ArrayList<>();
        fileAdapter = new FileAdapter(this, fileList);

        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFiles.setAdapter(fileAdapter);

        fabAddFile.setOnClickListener(view -> selectFile());
    }

    private void loadFilesFromFirestore() {
        filesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                fileList.clear();
                for (DocumentSnapshot snapshot : task.getResult()) {
                    FileItem fileItem = snapshot.toObject(FileItem.class);
                    if (fileItem != null) {
                        fileList.add(fileItem);
                        Log.d("FolderFilesActivity", "File added: " + fileItem.getFileName());
                    } else {
                        Log.e("FolderFilesActivity", "File data missing for ID: " + snapshot.getId());
                    }
                }
                fileAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(FolderFilesActivity.this, "Failed to load files", Toast.LENGTH_SHORT).show();
                Log.e("FolderFilesActivity", "Error loading files: " + task.getException().getMessage());
            }
        });
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null && isValidFileType(fileUri)) {
                // Show a dialog to input a new name
                showRenameDialog(fileUri);
            } else {
                showErrorMessage("Invalid file format. Select a PDF, image, or Word file.");
            }
        }
    }

    private void showRenameDialog(Uri fileUri) {
        // Create a simple dialog to input a new file name
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename File");

        // Create an EditText for the user to enter the new file name
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newFileName = input.getText().toString().trim();
            if (newFileName.isEmpty()) {
                Toast.makeText(this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                uploadFileWithNewName(fileUri, newFileName);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void uploadFileWithNewName(Uri fileUri, String newFileName) {
        if (fileUri == null) return;

        // Get a new file ID
        String fileId = filesRef.document().getId();

        // Update the file name in Firebase Storage (you may want to delete the old file, but here we just upload as a new file)
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("files/" + fileId);

        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String fileType = getContentResolver().getType(fileUri);
                    Timestamp uploadedAt = Timestamp.now();

                    // Create FileItem with new file name
                    FileItem fileItem = new FileItem(fileId, newFileName, downloadUri.toString(), uploadedAt, fileType);

                    // Update Firestore with new file name
                    filesRef.document(fileId).set(fileItem)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "File renamed and uploaded", Toast.LENGTH_SHORT).show();
                                loadFilesFromFirestore(); // Reload the file list to reflect changes
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add file metadata", Toast.LENGTH_SHORT).show());
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload file", Toast.LENGTH_SHORT).show());
    }

    private boolean isValidFileType(Uri fileUri) {
        String fileType = getContentResolver().getType(fileUri);
        return fileType != null && (fileType.startsWith("application/pdf") ||
                fileType.startsWith("image/") ||
                fileType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml.document"));
    }

/*
    private void uploadFile(Uri fileUri) {
        if (fileUri == null) return;

        String fileId = filesRef.document().getId(); // Generates a new document ID
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("files/" + fileId);

        storageReference.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String fileName = fileUri.getLastPathSegment();
                    String fileType = getContentResolver().getType(fileUri);
                    Timestamp uploadedAt = Timestamp.now();

                    FileItem fileItem = new FileItem(fileId, fileName, downloadUri.toString(), uploadedAt, fileType);

                    filesRef.document(fileId).set(fileItem)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "File added", Toast.LENGTH_SHORT).show();
                                loadFilesFromFirestore();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to add file metadata", Toast.LENGTH_SHORT).show());
                }).addOnFailureListener(e -> Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload file", Toast.LENGTH_SHORT).show());
    }
*/

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.e("FolderFilesActivity", message);
    }
}
