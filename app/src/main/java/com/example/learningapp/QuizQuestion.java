package com.example.learningapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Matches JSON: { "question": "...", "options": [...], "correct_answer": "A" }
 */
public class QuizQuestion {
    @SerializedName("question")
    private String question;

    @SerializedName("options")
    private List<String> options;

    // We’ll ignore correctAnswer for display
    @SerializedName("correct_answer")
    private String correctAnswer;

    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
    public String getCorrectAnswer() { return correctAnswer; }
}
