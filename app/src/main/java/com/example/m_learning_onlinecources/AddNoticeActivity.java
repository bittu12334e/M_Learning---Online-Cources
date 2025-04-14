package com.example.m_learning_onlinecources;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class AddNoticeActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private Button btnPickDate, btnAttachFile, btnSubmitNotice;
    private TextView tvSelectedDate, tvFileName;

    private String fileUrl;
    private long selectedTimestamp = 0;
    private StorageReference storageReference;

    public static final int PICK_FILE_REQUEST = 1;
    private ProgressDialog progressDialog;

    // Firestore reference
    private FirebaseFirestore firestore;
    private CollectionReference noticesCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_notice);

        // Initialize UI components
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        btnPickDate = findViewById(R.id.btn_pick_date);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        btnAttachFile = findViewById(R.id.btn_attach_file);
        tvFileName = findViewById(R.id.tv_file_name);
        btnSubmitNotice = findViewById(R.id.btn_submit_notice);

        // Initialize Firebase Storage and Firestore references
        storageReference = FirebaseStorage.getInstance().getReference("notice_files");
        firestore = FirebaseFirestore.getInstance();
        noticesCollection = firestore.collection("notices");

        // Pick Date functionality
        btnPickDate.setOnClickListener(v -> showDatePicker());

        // Attach File functionality
        btnAttachFile.setOnClickListener(v -> openFilePicker());

        // Submit Notice button
        btnSubmitNotice.setOnClickListener(v -> {
            if (fileUrl != null) {
                uploadFileToFirebase();
            } else {
                addNoticeToFirestore(null, null); // Add notice without file
            }
        });
    }

    // Show date picker
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);

            selectedTimestamp = selectedDate.getTimeInMillis(); // Save selected date as timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            tvSelectedDate.setText(sdf.format(selectedDate.getTime())); // Display selected date
        }, year, month, day);
        datePickerDialog.show();
    }

    // Open file picker to allow selecting any file
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData(); // Get file URI
            fileUrl = fileUri.toString(); // Save the URI as a string
            String fileName = getFileName(fileUri);
            tvFileName.setText(fileName); // Display selected file name
        }
    }

    // Get file name from URI
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex >= 0) {
                    result = cursor.getString(nameIndex);
                } else {
                    result = "Unknown File Name"; // Default value if not found
                }
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // Upload file to Firebase Storage
    private void uploadFileToFirebase() {
        if (fileUrl == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading file...");
        progressDialog.show();

        Uri uri = Uri.parse(fileUrl); // Convert String back to Uri for Firebase upload
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "_" + getFileName(uri));

        // Upload the file
        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    dismissProgressDialog();
                    String uploadedFileUrl = downloadUri.toString();
                    String fileType = getContentResolver().getType(uri); // Get MIME type of the file
                    addNoticeToFirestore(uploadedFileUrl, fileType); // Save notice with file URL and file type
                }))
                .addOnFailureListener(e -> {
                    dismissProgressDialog();
                    Toast.makeText(AddNoticeActivity.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Dismiss the progress dialog
    private void dismissProgressDialog() {
        if (!isFinishing() && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // Add the notice to Firestore
    private void addNoticeToFirestore(String uploadedFileUrl, String fileType) {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validate input fields
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Default to current timestamp if no date is selected
        if (selectedTimestamp == 0) {
            selectedTimestamp = System.currentTimeMillis();
        }

        // Create a unique notice ID
        String noticeId = noticesCollection.document().getId();
        Notice notice = new Notice(noticeId, title, description, FirebaseAuth.getInstance().getCurrentUser().getUid(), selectedTimestamp, uploadedFileUrl, fileType);

        // Save the notice in Firestore
        noticesCollection.document(noticeId)
                .set(notice)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AddNoticeActivity.this, "Notice added successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Return to the previous screen
                    } else {
                        Toast.makeText(AddNoticeActivity.this, "Failed to add notice", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
