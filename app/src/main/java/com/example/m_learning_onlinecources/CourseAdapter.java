package com.example.m_learning_onlinecources;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {

    private List<Course> courseList;
    private Context context;

    public CourseAdapter(List<Course> courseList, Context context) {
        this.courseList = courseList;
        this.context = context;
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_item, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courseList.get(position);

        // Bind data to the ViewHolder
        holder.title.setText(course.getTitle());
        holder.name.setText(course.getName());
        holder.description.setText(course.getDescription());
        //holder.courseUploadedBy.setText("Uploaded by: " + (course.getUploadedBy() != null ? course.getUploadedBy() : "Unknown"));

        // Navigate to UploadVideosActivity on click
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, EditCourseDetailActivity.class);
            intent.putExtra("courseId", course.getId()); // Pass the course ID
            intent.putExtra("courseName", course.getName());
            intent.putExtra("courseTitle", course.getTitle()); // Pass the course title
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return courseList.size();
    }

    public static class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView name, title, description, courseUploadedBy;

        public CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.courseName);
            title = itemView.findViewById(R.id.courseTitle);
            description = itemView.findViewById(R.id.courseDescription);
            //courseUploadedBy = itemView.findViewById(R.id.courseUploadedBy);
        }
    }
}
