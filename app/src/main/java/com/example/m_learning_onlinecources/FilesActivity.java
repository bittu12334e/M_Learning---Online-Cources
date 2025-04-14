package com.example.m_learning_onlinecources;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FilesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFiles;
    private FilesAdapter filesAdapter;
    private List<FileItem> fileList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        recyclerViewFiles = findViewById(R.id.recyclerViewFiles);
        recyclerViewFiles.setLayoutManager(new LinearLayoutManager(this));

        fileList = new ArrayList<>();
        filesAdapter = new FilesAdapter(this, fileList);
        recyclerViewFiles.setAdapter(filesAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Get folderId from the intent
        String folderId = getIntent().getStringExtra("folderId");
        Log.d("FilesActivity", "Folder ID: " + folderId);

        if (folderId != null) {
            fetchFilesForFolder(folderId);
        } else {
            Toast.makeText(this, "Invalid folder ID", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void fetchFilesForFolder(String folderId) {
        firestore.collection("folders_files")
                .document(folderId)
                .collection("files")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fileList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            FileItem fileItem = doc.toObject(FileItem.class);
                            if (fileItem != null) {
                                fileList.add(fileItem);
                            }
                        }

                        Log.d("FilesActivity", "Files fetched: " + fileList.size());

                        if (fileList.isEmpty()) {
                            Toast.makeText(this, "No files found in this folder", Toast.LENGTH_SHORT).show();
                        }
                        filesAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to fetch files", Toast.LENGTH_SHORT).show();
                        Log.e("FilesActivity", "Error fetching files", task.getException());
                    }
                });
    }
}
