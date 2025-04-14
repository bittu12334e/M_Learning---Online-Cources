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

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private final List<FileItem> fileList;
    private final Context context;

    public FilesAdapter(Context context, List<FileItem> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new FileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileItem fileItem = fileList.get(position);

        // Format upload date if available
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
        if (fileItem.getUploadedAt() != null) {
            holder.tvUploadDate.setText("Uploaded on: " + sdf.format(fileItem.getUploadedAt().toDate()));
        } else {
            holder.tvUploadDate.setText("Uploaded on: N/A");
        }

        holder.fileName.setText(fileItem.getFileName());

        // Set click listener for viewing the file
        holder.btnViewFile.setOnClickListener(view -> {
            String fileUrl = fileItem.getFileUrl();
            String fileType = fileItem.getFileType() != null ? fileItem.getFileType() : getMimeType(fileUrl);

            if (fileUrl != null && fileType != null) {
                openFile(fileUrl, fileType);
            } else {
                Toast.makeText(context, "File URL or type is missing", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
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

    static class FileViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, tvUploadDate;
        Button btnViewFile;

        FileViewHolder(View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.file_name);
            tvUploadDate = itemView.findViewById(R.id.tvUploadDate);
            btnViewFile = itemView.findViewById(R.id.btnViewFile);
        }
    }
}
