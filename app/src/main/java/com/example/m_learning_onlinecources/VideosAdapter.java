package com.example.m_learning_onlinecources;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideosAdapter<T> extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    private List<T> videoList;

    public VideosAdapter(List<T> videoList) {
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        T video = videoList.get(position);

        if (video instanceof Course.Video) {
            holder.txtVideoTitle.setText(((Course.Video) video).getTitle());
        } else if (video instanceof Video) {
            holder.txtVideoTitle.setText(((Video) video).getTitle());
        }

        holder.btnWatch.setOnClickListener(v -> {
            if (video instanceof Course.Video) {
                String videoUrl = ((Course.Video) video).getUrl();
                playVideo(holder.itemView.getContext(), videoUrl);
            } else if (video instanceof Video) {
                String videoUrl = ((Video) video).getUrl();
                playVideo(holder.itemView.getContext(), videoUrl);
            }
        });

    }

    private void playVideo(Context context, String videoUrl) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra("videoUrl", videoUrl);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView txtVideoTitle;
        Button btnWatch;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtVideoTitle = itemView.findViewById(R.id.txt_video_title);
            btnWatch = itemView.findViewById(R.id.btn_watch_video);
        }
    }
}
