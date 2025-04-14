package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
public class CoursesAdapter extends RecyclerView.Adapter<CoursesAdapter.CourseViewHolder> {

    private List<Course> courses;
    private String currentStudentId;
    private OnJoinCourseListener listener;


    public CoursesAdapter(List<Course> courses, OnJoinCourseListener listener, String currentStudentId) {
        this.courses = courses;
        this.listener = listener;
        this.currentStudentId = currentStudentId;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        if (course == null) return;

        holder.txtCourseName.setText(course.getName() != null ? course.getName() : "N/A");
        holder.txtVideoCount.setText("Videos: " + (course.getVideos() != null ? course.getVideos().size() : 0));

        // Check if the user is already enrolled
        DatabaseReference enrollmentRef = FirebaseDatabase.getInstance()
                .getReference("enrollments")
                .child(currentStudentId)
                .child("courses")
                .child(course.getId());

        enrollmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.btnJoin.setText("Pursuing");
                    holder.btnJoin.setOnClickListener(v -> {
                        Intent intent = new Intent(holder.itemView.getContext(), VideoListActivity.class);
                        intent.putExtra("courseId", course.getId());
                        holder.itemView.getContext().startActivity(intent);
                    });
                } else {
                    holder.btnJoin.setText("Join Course");
                    holder.btnJoin.setEnabled(true);

                    // Handle join course action
                    holder.btnJoin.setOnClickListener(v -> {
                        listener.onJoinCourse(course);

                        // Save enrollment in the database
                        enrollmentRef.setValue(course)
                                .addOnSuccessListener(unused -> {
                                    holder.btnJoin.setText("Pursuing");
                                    holder.btnJoin.setEnabled(false);
                                    Toast.makeText(v.getContext(), "You are now pursuing this course!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(v.getContext(), "Error joining course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(holder.itemView.getContext(), "Error checking enrollment: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return courses.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView txtCourseName, txtVideoCount;
        Button btnJoin;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCourseName = itemView.findViewById(R.id.txt_course_name);
            txtVideoCount = itemView.findViewById(R.id.txt_video_count);
            btnJoin = itemView.findViewById(R.id.btn_join_course);
        }
    }

    public interface OnJoinCourseListener {
        void onJoinCourse(Course course);
    }
}
