package com.example.learningapp;

/** Model for a single task summary card */
public class TaskSummary {
    private final String id;
    private final String title;
    private final String description;

    public TaskSummary(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
