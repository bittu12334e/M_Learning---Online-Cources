package com.example.m_learning_onlinecources;

public class Teacher {
    private String name;
    private String email;
    private String phone;
    private String designation;
    private String userId;
    private String password;

    public Teacher() {

    }

    public Teacher(String name, String email, String phone,
                   String designation, String userId, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.designation = designation;
        this.userId = userId;
        this.password = password;
    }


    // Add getters and setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }
}
