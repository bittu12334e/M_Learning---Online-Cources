package com.example.m_learning_onlinecources;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AdminViewStudents extends AppCompatActivity {

    private RecyclerView rvStudents;
    private TextView tvNoStudents;
    private FirebaseFirestore db;
    private List<Student> studentList;
    private StudentAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_view_students);

        rvStudents = findViewById(R.id.rv_students);
        tvNoStudents = findViewById(R.id.tv_no_students);

        db = FirebaseFirestore.getInstance();

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(this, studentList);

        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(studentAdapter);

        fetchStudents();
    }

    private void fetchStudents() {
        db.collection("students")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            studentList.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                Student student = document.toObject(Student.class);
                                studentList.add(student);
                            }
                            studentAdapter.notifyDataSetChanged();

                            if (studentList.isEmpty()) {
                                tvNoStudents.setVisibility(View.VISIBLE);
                            } else {
                                tvNoStudents.setVisibility(View.GONE);
                            }
                        } else {
                            tvNoStudents.setVisibility(View.VISIBLE);
                            Toast.makeText(AdminViewStudents.this, "Failed to fetch students", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
