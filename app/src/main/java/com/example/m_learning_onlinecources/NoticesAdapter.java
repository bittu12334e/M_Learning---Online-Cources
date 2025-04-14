package com.example.m_learning_onlinecources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoticesAdapter extends RecyclerView.Adapter<NoticesAdapter.NoticesViewHolder> {

    private List<Notice> noticeList;
    private OnFileClickListener fileClickListener;

    public NoticesAdapter(List<Notice> noticeList, OnFileClickListener fileClickListener) {
        this.noticeList = noticeList;
        this.fileClickListener = fileClickListener;
    }

    @NonNull
    @Override
    public NoticesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notices, parent, false);
        return new NoticesViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NoticesViewHolder holder, int position) {
        Notice notice = noticeList.get(position);

        holder.txtTitle.setText(notice.getTitle());
        holder.txtDescription.setText(notice.getDescription());

        holder.btnOpenFile.setOnClickListener(v -> {
            if (fileClickListener != null) {
                fileClickListener.onFileClick(notice.getFileUrl(), notice.getFileType());
            }
        });
    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    public static class NoticesViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtDescription;
        Button btnOpenFile;

        public NoticesViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txt_notice_title);
            txtDescription = itemView.findViewById(R.id.txt_notice_description);
            btnOpenFile = itemView.findViewById(R.id.btn_open_file);
        }


    }

    public interface OnFileClickListener {
        void onFileClick(String fileUrl, String fileType);
    }
}
