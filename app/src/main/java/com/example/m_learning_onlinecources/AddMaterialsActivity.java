package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddMaterialsActivity extends AppCompatActivity implements FolderAdapter.OnFolderClickListener {

    private RecyclerView recyclerViewFolders;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;
    private DatabaseReference foldersRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_materials);

        recyclerViewFolders = findViewById(R.id.recyclerViewFolders);
        FloatingActionButton fabAddFolder = findViewById(R.id.fab_add_folder);

        // Initialize folder list and adapter
        folderList = new ArrayList<>();
        folderAdapter = new FolderAdapter(folderList, this);
        recyclerViewFolders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFolders.setAdapter(folderAdapter);

        // Firebase reference for folders
        foldersRef = FirebaseDatabase.getInstance().getReference("folders");

        // Set click listener for the FAB to add a new folder
        fabAddFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFolderDialog();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Load folders from Firebase
        loadFoldersFromFirebase();
    }

    // Show dialog to get the folder name from the user
    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Folder");

        // Set up input field
        final EditText input = new EditText(this);
        input.setHint("Folder Name");
        builder.setView(input);

        // Set up buttons
        builder.setPositiveButton("Add", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                addFolderToFirebase(folderName);
            } else {
                Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // Add folder with current date and time to Firebase
    private void addFolderToFirebase(String folderName) {
        String dateTime = getCurrentDateTime();
        String folderId = foldersRef.push().getKey();
        Folder folder = new Folder(folderId, folderName, dateTime);

        if (folderId != null) {
            foldersRef.child(folderId).setValue(folder)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Folder added", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to add folder", Toast.LENGTH_SHORT).show());
        }
    }

    // Get the current date and time in a specific format
    private String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    // Load folders from Firebase
    private void loadFoldersFromFirebase() {
        foldersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                folderList.clear(); // Clear existing list to avoid duplicates
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Folder folder = snapshot.getValue(Folder.class);
                    if (folder != null) {
                        // Set folder creation time
                        Timestamp folderCreationTime = snapshot.child("createdAt").getValue(Timestamp.class);
                        if (folderCreationTime != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                            String creationDateStr = "Created on: " + sdf.format(folderCreationTime.toDate());
                            folder.setCreationDate(creationDateStr);
                        }
                        folderList.add(folder); // Add folder to list
                    }
                }
                folderAdapter.notifyDataSetChanged(); // Notify adapter to refresh RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddMaterialsActivity.this, "Failed to load folders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFolderClick(Folder folder) {

        Intent intent = new Intent(this, FolderFilesActivity.class);
        intent.putExtra("FOLDER_ID", folder.getId());
        startActivity(intent);
    }

}



