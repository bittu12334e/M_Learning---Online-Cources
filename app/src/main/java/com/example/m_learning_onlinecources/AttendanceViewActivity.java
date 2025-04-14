package com.example.m_learning_onlinecources;

import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceViewActivity extends AppCompatActivity {
    private TextView attendanceDetails;
    CalendarView calendarView;
    private static final String TAG = "AttendanceViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_view);
        attendanceDetails = findViewById(R.id.attendanceDetails);
        calendarView = findViewById(R.id.calendarView);

        // Set up a listener for date changes
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            Log.d(TAG, "Selected date: " + selectedDate);

            // Fetch attendance for the selected date
            //fetchAttendanceDate(selectedDate);
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String studentId = auth.getCurrentUser().getUid();
            String userEmail = auth.getCurrentUser().getEmail();

            Log.d(TAG, "Current User UID: " + studentId);
            Log.d(TAG, "Current User Email: " + userEmail);

            fetchStudentName(studentId);
        } else {
            Log.e(TAG, "No user is currently logged in");
            attendanceDetails.setText("Please log in to view attendance.");
        }
    }

    private void fetchStudentName(String studentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        Log.d(TAG, "Attempting to fetch student with userId: " + studentId);

        firestore.collection("students")
                .whereEqualTo("userId", studentId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String documentId = querySnapshot.getDocuments().get(0).getId();
                        Log.d(TAG, "Found student document ID: " + documentId);

                        Map<String, Object> data = querySnapshot.getDocuments().get(0).getData();
                        Log.d(TAG, "Student data: " + data);

                        fetchAttendanceDetails(documentId);
                    } else {
                        Log.w(TAG, "No student found with userId: " + studentId);
                        attendanceDetails.setText("No student record found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching student by userId: ", e);
                    attendanceDetails.setText("Error fetching student information.");
                });
    }

    private void fetchAttendanceDetails(String studentDocumentId) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("students")
                .document(studentDocumentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String studentName = documentSnapshot.getString("name");
                        Log.d(TAG, "Retrieved student name from document: '" + studentName + "'");

                        if (studentName != null) {
                            // Remove any leading/trailing whitespace
                            studentName = studentName.trim();
                            fetchAttendanceByStudentName(studentName);
                        } else {
                            Log.e(TAG, "Student name is null in document");
                            attendanceDetails.setText("Error: Student name not found.");
                        }
                    } else {
                        Log.e(TAG, "Student document doesn't exist");
                        attendanceDetails.setText("Error: Student document not found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching student details: ", e);
                    attendanceDetails.setText("Error fetching student details.");
                });
    }

    private void fetchAttendanceByStudentName(String studentName) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("attendance");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Date> attendedDates = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        // Check if student attended
                        DataSnapshot studentsSnapshot = snapshot.child("students").child(studentName);
                        if (studentsSnapshot.exists() && Boolean.TRUE.equals(studentsSnapshot.child("attendance").getValue(Boolean.class))) {
                            // Get the date string
                            String dateStr = snapshot.child("date").getValue(String.class);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                            try {
                                Date date = sdf.parse(dateStr);
                                if (date != null) {
                                    attendedDates.add(date);
                                }
                            } catch (ParseException e) {
                                Log.e(TAG, "Error parsing date: " + dateStr, e);
                            }
                        }
                    }

                    // Update calendar with attended dates
                    if (!attendedDates.isEmpty()) {
                        // Set up calendar decorator for attended dates
                        setupCalendarWithAttendance(attendedDates);
                    } else {
                        Toast.makeText(AttendanceViewActivity.this,
                                "No attendance records found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error fetching attendance: ", databaseError.toException());
                Toast.makeText(AttendanceViewActivity.this,
                        "Error fetching attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCalendarWithAttendance(List<Date> attendedDates) {
        CalendarView calendarView = findViewById(R.id.calendarView);

        // Set date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);

            // Check if this date is in attended dates
            boolean wasPresent = false;
            for (Date attendedDate : attendedDates) {
                Calendar attendedCal = Calendar.getInstance();
                attendedCal.setTime(attendedDate);

                if (attendedCal.get(Calendar.YEAR) == year &&
                        attendedCal.get(Calendar.MONTH) == month &&
                        attendedCal.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                    wasPresent = true;
                    break;
                }
            }

            // Show attendance status for selected date
            if (wasPresent) {
                attendanceDetails.setText("Present on " +
                        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selectedDate.getTime()));
            } else {
                attendanceDetails.setText("No attendance record for " +
                        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selectedDate.getTime()));
            }
        });

        // Set initial date to first attended date if available
        if (!attendedDates.isEmpty()) {
            calendarView.setDate(attendedDates.get(0).getTime());
        }
    }
    /*
    private void setupCalendarWithAttendance(List<Date> attendedDates) {
        CalendarView calendarView = findViewById(R.id.calendarView);

        // Set date change listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);

            // Check if this date is in attended dates
            boolean wasPresent = false;
            for (Date attendedDate : attendedDates) {
                Calendar attendedCal = Calendar.getInstance();
                attendedCal.setTime(attendedDate);

                if (attendedCal.get(Calendar.YEAR) == year &&
                        attendedCal.get(Calendar.MONTH) == month &&
                        attendedCal.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                    wasPresent = true;
                    break;
                }
            }

            // Show attendance status for selected date
            if (wasPresent) {
                attendanceDetails.setText("Present on " +
                        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selectedDate.getTime()));
            } else {
                attendanceDetails.setText("No attendance record for " +
                        new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(selectedDate.getTime()));
            }
        });

        // Set initial date to first attended date if available
        if (!attendedDates.isEmpty()) {
            calendarView.setDate(attendedDates.get(0).getTime());
        }
    }
*/


}
