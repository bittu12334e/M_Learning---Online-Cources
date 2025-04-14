package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NoticeViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private NoticesAdapter noticeAdapter;
    private List<Notice> noticeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_view);

        recyclerView = findViewById(R.id.recycler_notices);
        progressBar = findViewById(R.id.progress_bar);

        noticeList = new ArrayList<>();
        noticeAdapter = new NoticesAdapter(noticeList, this::openFile);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noticeAdapter);

        fetchNotices();
    }

    private void fetchNotices() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("notices")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Notice notice = document.toObject(Notice.class);
                        noticeList.add(notice);
                    }
                    noticeAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("NoticeListActivity", "Error fetching notices", e);
                });
    }

    private void openFile(String fileUrl, String fileType) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(fileUrl), fileType);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Log.e("NoticeListActivity", "Error opening file: " + fileUrl, e);
        }
    }
}
