package com.example.m_learning_onlinecources;


public class Student {

    private String name;
    private String studentId;
    private String userId;
    private String email;
    private String password;
    private boolean attendance;
    private String gender;
    private String phone;
    private String dob;
    private String profile_picture_url;

    private String classs,section,roll;



    // Default constructor (required for Firebase)
    public Student() {

    }

    // Full constructor
    public Student(String name, String studentId, String email, String password, String userId, boolean attendance) {
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.attendance = attendance;
    }

    public String getClasss() {
        return classs;
    }

    public void setClasss(String classs) {
        this.classs = classs;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getters
    public String getUserId() { return userId; }

    public String getName() { return name; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getDob() { return dob; }
    public String getProfile_picture_url() { return profile_picture_url; }
    public boolean isAttendance() { return attendance; }

    // Setters (if needed)
    public void setUserId(String userId) { this.userId = userId; }


    public void setName(String name) { this.name = name; }
    public void setGender(String gender) { this.gender = gender; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setDob(String dob) { this.dob = dob; }
    public void setProfile_picture_url(String profile_picture_url) { this.profile_picture_url = profile_picture_url; }
    public void setAttendance(boolean attendance) { this.attendance = attendance; }
    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }
}
