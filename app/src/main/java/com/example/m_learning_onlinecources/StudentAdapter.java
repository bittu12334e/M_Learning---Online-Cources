package com.example.m_learning_onlinecources;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<Student> studentList; // Using Student class for uniformity
    private Context context;

    // Constructor for the adapter, taking in the context and the list of students
    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
    }

    // Inflating the layout for individual student items
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    // Binding data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = studentList.get(position);

        // Setting the values for name, email, and student ID
        holder.tvName.setText(student.getName());
        holder.tvEmail.setText(student.getEmail());
        holder.tvStudentId.setText(student.getStudentId());
    }

    // Getting the total number of students in the list
    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // ViewHolder to manage individual item views
    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail, tvStudentId; // TextViews for displaying student details

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initializing the TextViews from the item layout
            tvName = itemView.findViewById(R.id.tv_student_name);
            tvEmail = itemView.findViewById(R.id.tv_student_email);
            tvStudentId = itemView.findViewById(R.id.tv_student_id);
        }
    }
}
