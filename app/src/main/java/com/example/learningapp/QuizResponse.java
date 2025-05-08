package com.example.learningapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Wrapper for the JSON returned by /getQuiz:
 * {
 *   "quiz": [
 *     { "question":"…", "options":[…], "correct_answer":"…" },
 *     …
 *   ]
 * }
 */
public class QuizResponse {
    @SerializedName("quiz")
    private List<QuizQuestion> quiz;

    public List<QuizQuestion> getQuiz() {
        return quiz;
    }
}
