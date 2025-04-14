package com.example.m_learning_onlinecources;
public class Folder {
    private String id;
    private String name;
    private String creationDate;


    // Default constructor required for calls to DataSnapshot.getValue(Folder.class)
    public Folder() {
    }

    // Constructor with parameters
    public Folder(String id, String name, String creationDate) {
        this.id = id;
        this.name = name;

        this.creationDate = creationDate;

    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getCreationDate() {
        return creationDate;
    }

}
