package com.example.m_learning_onlinecources;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminViewTeachers extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvNoTeachers;
    private FirebaseFirestore db;
    private TeachersAdapter adapter;
    private List<Teacher> teacherList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_teachers);

        recyclerView = findViewById(R.id.rv_teachers);
        tvNoTeachers = findViewById(R.id.tv_no_teachers);

        db = FirebaseFirestore.getInstance();
        teacherList = new ArrayList<>();
        adapter = new TeachersAdapter(teacherList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTeachersFromFirestore();
    }

    private void loadTeachersFromFirestore() {
        db.collection("teachers")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Teacher teacher = document.toObject(Teacher.class);
                            teacherList.add(teacher);
                        }
                        adapter.notifyDataSetChanged();
                        tvNoTeachers.setVisibility(View.GONE);
                    } else {
                        // Show "No teacher available" message
                        tvNoTeachers.setVisibility(View.VISIBLE);
                    }
                });
    }
}
