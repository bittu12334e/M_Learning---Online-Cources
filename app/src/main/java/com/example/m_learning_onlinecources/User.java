package com.example.m_learning_onlinecources;
public class User {
    private String name;
    private String phoneNumber;
    private String email;
    private String role;
    private String gender;
    private String dob;
    private String salt;
    private String hashedPassword;

    public User(String name, String phoneNumber, String email, String role, String gender, String dob, String imageUrl) {
    }

    public User(String name, String phoneNumber, String email, String role, String gender, String dob, String salt, String hashedPassword) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
        this.gender = gender;
        this.dob = dob;
        this.salt = salt;
        this.hashedPassword = hashedPassword;
    }

    // Getters and setters for each property
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
}