package com.example.m_learning_onlinecources;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private VideosAdapter<Video> videosAdapter;
    private List<Video> videoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        recyclerView = findViewById(R.id.recycler_videos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String courseId = getIntent().getStringExtra("courseId");
        fetchVideos(courseId);
    }

    private void fetchVideos(String courseId) {
        FirebaseDatabase.getInstance()
                .getReference("courses")
                .child(courseId)
                .child("videos")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        videoList.clear();
                        for (DataSnapshot videoSnapshot : snapshot.getChildren()) {
                            Video video = videoSnapshot.getValue(Video.class);
                            if (video != null) {
                                videoList.add(video);
                            }
                        }
                        videosAdapter = new VideosAdapter<>(videoList);
                        recyclerView.setAdapter(videosAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VideoListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
