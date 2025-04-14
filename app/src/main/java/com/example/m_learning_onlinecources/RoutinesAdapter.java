package com.example.m_learning_onlinecources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

public class RoutinesAdapter extends RecyclerView.Adapter<RoutinesAdapter.RoutinesViewHolder> {

    private List<Routine> routineList;
    private OnRoutineClickListener listener;

    public RoutinesAdapter(List<Routine> routineList, OnRoutineClickListener listener) {
        this.routineList = routineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoutinesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routines, parent, false);
        return new RoutinesViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull RoutinesViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        holder.txtRoutineTitle.setText(routine.getTitle());
        // Set uploaded date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        holder.txtUploadedDate.setText("Uploaded Date: " + sdf.format(routine.getUploadedAt().toDate()));

        // Handle file opening logic
        holder.btnOpenFile.setOnClickListener(v -> listener.onRoutineClick(routine.getFileUrl(), routine.getFileType()));
    }

    @Override
    public int getItemCount() {
        return routineList.size();
    }

    public static class RoutinesViewHolder extends RecyclerView.ViewHolder {
        TextView txtRoutineTitle;
        TextView txtUploadedDate;

        Button btnOpenFile;

        public RoutinesViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRoutineTitle = itemView.findViewById(R.id.txt_routine_title);
            txtUploadedDate = itemView.findViewById(R.id.txt_uploaded_date);
            btnOpenFile = itemView.findViewById(R.id.btn_open_file);
        }
    }

    public interface OnRoutineClickListener {
        void onRoutineClick(String fileUrl, String fileType);
    }
}
