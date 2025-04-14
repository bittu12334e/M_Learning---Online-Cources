package com.example.m_learning_onlinecources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<Student> studentList;

    public AttendanceAdapter(List<Student> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.studentName.setText(student.getName());
        holder.studentRoll.setText(student.getRoll());
        holder.attendanceCheckBox.setChecked(student.isAttendance());

        // Update attendance status when checkbox is clicked
        holder.attendanceCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            student.setAttendance(isChecked);
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView studentName , studentRoll;
        CheckBox attendanceCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.students_name);
            studentRoll = itemView.findViewById(R.id.students_roll);
            attendanceCheckBox = itemView.findViewById(R.id.cb_attendance);
        }
    }
}
