package com.example.m_learning_onlinecources;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewNoticesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoticeAdapter noticeAdapter;
    private List<Notice> noticeList;

    private FirebaseFirestore firestore;
    private CollectionReference noticesCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_notices);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noticeList = new ArrayList<>();
        noticeAdapter = new NoticeAdapter(noticeList, this::editNotice);
        recyclerView.setAdapter(noticeAdapter);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        noticesCollection = firestore.collection("notices");

        fetchNotices();
    }

    private void fetchNotices() {
        noticesCollection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot value, FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(ViewNoticesActivity.this, "Failed to retrieve notices", Toast.LENGTH_SHORT).show();
                    return;
                }

                noticeList.clear();
                if (value != null) {
                    for (QueryDocumentSnapshot document : value) {
                        Notice notice = document.toObject(Notice.class);
                        noticeList.add(notice);
                    }
                    noticeAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Method to handle edit notice action
    private void editNotice(Notice notice) {
        Intent intent = new Intent(this, EditNoticeActivity.class);
        intent.putExtra("NOTICE_ID", notice.getNoticeId());
        startActivity(intent);
    }
}
