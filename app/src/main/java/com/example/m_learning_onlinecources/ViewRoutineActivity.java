package com.example.m_learning_onlinecources;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewRoutineActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RoutineAdapter routineAdapter;
    private List<Routine> routineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_routine);

        recyclerView = findViewById(R.id.recyclerViewRoutines);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        routineList = new ArrayList<>();
        routineAdapter = new RoutineAdapter(this, routineList);
        recyclerView.setAdapter(routineAdapter);

        fetchRoutinesFromFirestore();
    }

    // Method to fetch routines from Firestore
    private void fetchRoutinesFromFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("routines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    routineList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Routine routine = document.toObject(Routine.class);
                        routine.setId(document.getId()); // Ensure the routine ID is set
                        routineList.add(routine);
                    }
                    routineAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("ViewRoutineActivity", "Error fetching routines", e);
                    Toast.makeText(ViewRoutineActivity.this, "Failed to load routines", Toast.LENGTH_SHORT).show();
                });
    }
}
