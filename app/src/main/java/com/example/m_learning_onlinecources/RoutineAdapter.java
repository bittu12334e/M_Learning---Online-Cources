package com.example.m_learning_onlinecources;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {
    private List<Routine> routineList;
    private Context context;

    public RoutineAdapter(Context context, List<Routine> routineList) {
        this.context = context;
        this.routineList = routineList;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        holder.tvUploadDate.setText("Uploaded on: " + sdf.format(routine.getUploadedAt().toDate()));
        holder.tvTitle.setText("Title : "+routine.getTitle());

        holder.btnViewFile.setOnClickListener(view -> {
            String fileUrl = routine.getFileUrl();
            String fileType = routine.getFileType();
            if (fileType == null) {
                fileType = getMimeType(fileUrl);
            }

            if (fileUrl != null && fileType != null) {
                openFile(fileUrl, fileType);
            } else {
                Toast.makeText(context, "File URL or type is missing", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnEditRoutine.setOnClickListener(view -> {
            Intent editIntent = new Intent(context, EditRoutineActivity.class);
            editIntent.putExtra("oldFileUrl", routine.getFileUrl());
            editIntent.putExtra("routineId", routine.getId());
            context.startActivity(editIntent);
        });
    }

    @Override
    public int getItemCount() {
        return routineList.size();
    }

    private void openFile(String fileUrl, String fileType) {
        Uri fileUri = Uri.parse(fileUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, fileType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView tvUploadDate,tvTitle;
        Button btnViewFile, btnEditRoutine;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRoutineTitle);
            tvUploadDate = itemView.findViewById(R.id.tvUploadDate);
            btnViewFile = itemView.findViewById(R.id.btnViewFile);
            btnEditRoutine = itemView.findViewById(R.id.btnEditRoutine);
        }
    }
}
