package com.example.m_learning_onlinecources;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private final List<Notice> noticeList;
    private final OnNoticeClickListener listener;

    public interface OnNoticeClickListener {
        void onNoticeClick(Notice notice);
    }

    public NoticeAdapter(List<Notice> noticeList, OnNoticeClickListener listener) {
        this.noticeList = noticeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notice notice = noticeList.get(position);
        holder.tvTitle.setText(notice.getTitle());
        holder.tvDescription.setText(notice.getDescription());

        // Handle click event to edit the notice
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditNoticeActivity.class);
            intent.putExtra("noticeId", notice.getNoticeId());
            intent.putExtra("title", notice.getTitle());
            intent.putExtra("description", notice.getDescription());
            intent.putExtra("fileUrl", notice.getFileUrl());
            intent.putExtra("fileType", notice.getFileType()); // Pass file type to EditNoticeActivity
            v.getContext().startActivity(intent);
        });

        // Handle "View File" button visibility and functionality
        if (notice.getFileUrl() != null && !notice.getFileUrl().isEmpty()) {
            holder.btnViewFile.setVisibility(View.VISIBLE);

            // View File Button click listener
            holder.btnViewFile.setOnClickListener(v -> {
                String mimeType = notice.getFileType();
                if (mimeType == null || mimeType.isEmpty()) {
                    mimeType = getFileMimeType(notice.getFileUrl());
                }
                openFile(v, notice.getFileUrl(), mimeType);
            });
        } else {
            holder.btnViewFile.setVisibility(View.GONE);
        }
    }

    private String getFileMimeType(String fileUrl) {
        if (fileUrl.endsWith(".pdf")) return "application/pdf";
        if (fileUrl.endsWith(".jpg") || fileUrl.endsWith(".jpeg")) return "image/jpeg";
        if (fileUrl.endsWith(".png")) return "image/png";
        if (fileUrl.endsWith(".doc") || fileUrl.endsWith(".docx")) return "application/msword";
        if (fileUrl.endsWith(".xls") || fileUrl.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (fileUrl.endsWith(".mp4")) return "video/mp4";
        if (fileUrl.endsWith(".txt")) return "text/plain";
        return "*/*"; // Fallback for unknown types
    }



    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    private void openFile(View view, String fileUrl, String fileType) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(view.getContext(), "File URL is invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Log the file URL and type for debugging
            Log.d("FileOpen", "File URL: " + fileUrl);
            Log.d("FileOpen", "File MIME Type: " + fileType);

            // Create a URI from the file URL
            Uri fileUri = Uri.parse(fileUrl);

            // Create an Intent to view the file
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, fileType); // Set the MIME type
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Check if there's an app to handle the file
            if (view.getContext().getPackageManager().resolveActivity(intent, 0) != null) {
                view.getContext().startActivity(intent);
            } else {
                Toast.makeText(view.getContext(), "No application found to open this file type.", Toast.LENGTH_SHORT).show();
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(view.getContext(), "Unable to open file. No app found.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(view.getContext(), "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle, tvDescription;
        public View btnViewFile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notice_title);
            tvDescription = itemView.findViewById(R.id.tv_notice_description);
            btnViewFile = itemView.findViewById(R.id.btn_view_file);
        }
    }
}
