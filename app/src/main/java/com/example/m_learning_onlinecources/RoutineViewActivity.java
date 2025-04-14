package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RoutineViewActivity extends AppCompatActivity {

    private RecyclerView recyclerRoutines;
    private RoutinesAdapter routineAdapter;
    private List<Routine> routineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_view);

        recyclerRoutines = findViewById(R.id.recycler_routines);
        routineList = new ArrayList<>();
        routineAdapter = new RoutinesAdapter(routineList, this::openRoutineFile);

        recyclerRoutines.setLayoutManager(new LinearLayoutManager(this));
        recyclerRoutines.setAdapter(routineAdapter);

        fetchRoutines();
    }


    private void fetchRoutines() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("routines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Routine routine = document.toObject(Routine.class);
                        routineList.add(routine);
                    }
                    routineAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("RoutineListActivity", "Error fetching routines", e));
    }

    private void openRoutineFile(String fileUrl, String fileType) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(fileUrl), fileType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show();
            Log.e("NoticeListActivity", "Error opening file: " + fileUrl, e);
        }
    }

}
