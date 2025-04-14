package com.example.m_learning_onlinecources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TeachersAdapter extends RecyclerView.Adapter<TeachersAdapter.TeacherViewHolder> {

    private List<Teacher> teacherList;

    public TeachersAdapter(List<Teacher> teacherList) {
        this.teacherList = teacherList;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        Teacher teacher = teacherList.get(position);
        holder.tvName.setText(teacher.getName());
        holder.tvEmail.setText(teacher.getEmail());
        /*holder.tvPhone.setText(teacher.getPhone());*/
        holder.tvDesignation.setText(teacher.getDesignation());
    }

    @Override
    public int getItemCount() {
        return teacherList.size();
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvDesignation;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_teacher_name);
            tvEmail = itemView.findViewById(R.id.tv_teacher_email);
            /*tvPhone = itemView.findViewById(R.id.tv_teacher_phone);*/
            tvDesignation = itemView.findViewById(R.id.tv_teacher_designation);
        }
    }
}
