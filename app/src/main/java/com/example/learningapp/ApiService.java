package com.example.learningapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    /**
     * GET /getQuiz?topic=…
     * Returns a JSON object with a "quiz" array.
     */
    @GET("getQuiz")
    Call<QuizResponse> getQuiz(
            @Query("topic") String topic
    );
}
