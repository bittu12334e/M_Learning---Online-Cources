package com.example.m_learning_onlinecources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<Folder> folderList;
    private OnFolderClickListener onFolderClickListener;

    // Constructor
    public FolderAdapter(List<Folder> folderList, OnFolderClickListener listener) {
        this.folderList = folderList;
        this.onFolderClickListener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the updated card layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folderList.get(position);

        // Bind folder name and creation date
        holder.folderNameTextView.setText(folder.getName());
        holder.dateTimeTextView.setText(folder.getCreationDate());

        // Handle item click
        holder.itemView.setOnClickListener(v -> onFolderClickListener.onFolderClick(folder));
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    // ViewHolder class
    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderNameTextView;
        TextView dateTimeTextView;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            folderNameTextView = itemView.findViewById(R.id.folder_name);
            dateTimeTextView = itemView.findViewById(R.id.folder_creation_date_time);
        }
    }

    // Interface for click handling
    public interface OnFolderClickListener {
        void onFolderClick(Folder folder);
    }
}
