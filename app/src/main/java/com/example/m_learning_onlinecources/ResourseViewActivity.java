package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ResourseViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoldersAdapter folderAdapter;
    private List<Folder> folderList;
    private FirebaseDatabase realtimeDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resourse_view);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        folderList = new ArrayList<>();
        folderAdapter = new FoldersAdapter(folderList, this);
        recyclerView.setAdapter(folderAdapter);

        realtimeDb = FirebaseDatabase.getInstance();

        fetchFolders();
    }

    private void fetchFolders() {
        realtimeDb.getReference("folders").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                folderList.clear();
                for (DataSnapshot folderSnapshot : snapshot.getChildren()) {
                    String id = folderSnapshot.getKey();
                    String name = folderSnapshot.child("name").getValue(String.class);
                    String creationDate = folderSnapshot.child("creationDate").getValue(String.class);

                    if (id != null && name != null) {
                        folderList.add(new Folder(id, name, creationDate));
                    }
                }
                folderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResourseViewActivity.this, "Error fetching folders", Toast.LENGTH_SHORT).show();
                Log.e("ResourseViewActivity", "Error fetching folders", error.toException());
            }
        });
    }
}
