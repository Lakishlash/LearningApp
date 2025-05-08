package com.example.learningapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuizFragment extends Fragment {

    private static final String TAG = "QuizFragment";
    // Use 10.0.2.2 for Android emulator to connect to localhost on host machine
    private static final String API_URL = "http://10.0.2.2:5001/getQuiz?topic=movies";

    private TextView questionTextView;
    private RadioGroup optionsRadioGroup;
    private Button submitButton;
    private ProgressBar progressBar;
    private View loadingView;
    private View quizContent;

    private int currentQuestionIndex = 0;
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private int correctAnswers = 0;

    // QuizQuestion inner class
    private static class QuizQuestion {
        private final String question;
        private final List<String> options;
        private final String answer;

        public QuizQuestion(String question, List<String> options, String answer) {
            this.question = question;
            this.options = options;
            this.answer = answer;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getOptions() {
            return options;
        }

        public String getAnswer() {
            return answer;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        questionTextView = view.findViewById(R.id.questionTextView);
        optionsRadioGroup = view.findViewById(R.id.optionsRadioGroup);
        submitButton = view.findViewById(R.id.submitButton);
        loadingView = view.findViewById(R.id.loadingView);
        quizContent = view.findViewById(R.id.quizContent);
        progressBar = view.findViewById(R.id.progressBar);

        // Initially show loading and hide quiz content
        showLoading(true);

        // Set click listener for submit button
        submitButton.setOnClickListener(v -> submitAnswer());

        // Fetch quiz data from API
        fetchQuizData();
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null && quizContent != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            quizContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }

    private void fetchQuizData() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "API request failed", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to load quiz: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        showLoading(false);
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Unexpected code " + response);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Server error: " + response.code(), Toast.LENGTH_LONG).show();
                            showLoading(false);
                        });
                    }
                    return;
                }

                String responseData = response.body().string();
                Log.d(TAG, "API Response: " + responseData);

                try {
                    JSONObject jsonResponse = new JSONObject(responseData);
                    parseQuizData(jsonResponse);
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing JSON", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error parsing quiz data", Toast.LENGTH_LONG).show();
                            showLoading(false);
                        });
                    }
                }
            }
        });
    }

    private void parseQuizData(JSONObject jsonResponse) {
        try {
            quizQuestions.clear();

            // Check if the response has the expected structure
            if (jsonResponse.has("quiz")) {
                JSONArray quizArray = jsonResponse.getJSONArray("quiz");

                // Parse each question
                for (int i = 0; i < quizArray.length(); i++) {
                    JSONObject questionObj = quizArray.getJSONObject(i);

                    // Get question, options, and answer - handle different field names
                    String question = questionObj.getString("question");

                    // Check both "answer" and "correct_answer" fields
                    String answer = "A"; // Default value
                    if (questionObj.has("answer")) {
                        answer = questionObj.getString("answer");
                    } else if (questionObj.has("correct_answer")) {
                        answer = questionObj.getString("correct_answer");
                    }

                    JSONArray optionsArray = questionObj.getJSONArray("options");

                    List<String> options = new ArrayList<>();
                    for (int j = 0; j < optionsArray.length(); j++) {
                        options.add(optionsArray.getString(j));
                    }

                    quizQuestions.add(new QuizQuestion(question, options, answer));
                }

                // Display the first question
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        if (!quizQuestions.isEmpty()) {
                            displayQuestion(0);
                            updateProgressBar();
                        } else {
                            Toast.makeText(getContext(), "No questions available", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Invalid quiz format: " + jsonResponse.toString());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Invalid quiz format", Toast.LENGTH_SHORT).show();
                        showLoading(false);
                    });
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error processing quiz data", e);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Error processing quiz data", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }
    }

    private void displayQuestion(int index) {
        if (index < 0 || index >= quizQuestions.size()) {
            return;
        }

        QuizQuestion currentQuestion = quizQuestions.get(index);
        questionTextView.setText(currentQuestion.getQuestion());

        // Clear previous options
        optionsRadioGroup.removeAllViews();

        // Add radio buttons for each option
        List<String> options = currentQuestion.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setId(View.generateViewId());
            radioButton.setText(options.get(i));
            radioButton.setPadding(0, 16, 0, 16);
            optionsRadioGroup.addView(radioButton);
        }

        // Update submit button text on the last question
        if (index == quizQuestions.size() - 1) {
            submitButton.setText("Finish");
        } else {
            submitButton.setText("Submit");
        }
    }

    private void updateProgressBar() {
        if (progressBar != null) {
            progressBar.setMax(quizQuestions.size());
            progressBar.setProgress(currentQuestionIndex + 1);
        }
    }

    private void submitAnswer() {
        int selectedId = optionsRadioGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get the selected radio button and its index
            View radioButton = optionsRadioGroup.findViewById(selectedId);
            int radioIndex = optionsRadioGroup.indexOfChild(radioButton);

            // Check if answer is correct
            QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
            String correctAnswerLetter = currentQuestion.getAnswer();
            int correctIndex = "ABCD".indexOf(correctAnswerLetter);

            boolean isCorrect = radioIndex == correctIndex;
            if (isCorrect) {
                correctAnswers++;
            }

            // Show feedback
            Toast.makeText(getContext(),
                    isCorrect ? "Correct!" : "Incorrect. The answer is " + correctAnswerLetter,
                    Toast.LENGTH_SHORT).show();

            // Move to next question or finish quiz
            currentQuestionIndex++;
            if (currentQuestionIndex < quizQuestions.size()) {
                displayQuestion(currentQuestionIndex);
                updateProgressBar();
            } else {
                // Quiz finished - navigate to results or back to dashboard
                try {
                    // Try to navigate to results
                    showQuizResults();
                } catch (Exception e) {
                    // If navigation fails, at least go back to the dashboard
                    Log.e(TAG, "Failed to navigate to results: " + e.getMessage());
                    try {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_quizFragment_to_dashboardFragment);
                    } catch (Exception e2) {
                        Log.e(TAG, "Failed to navigate back: " + e2.getMessage());
                        // As a last resort, just show a toast
                        Toast.makeText(getContext(), "Quiz completed! Score: " + correctAnswers +
                                " out of " + quizQuestions.size(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in submit answer: " + e.getMessage());
            Toast.makeText(getContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showQuizResults() {
        // Create bundle with results data
        Bundle args = new Bundle();
        args.putInt("correctAnswers", correctAnswers);
        args.putInt("totalQuestions", quizQuestions.size());

        // Navigate to results fragment
        try {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_quizFragment_to_quizResultsFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed: " + e.getMessage());
            // Fall back to a simple toast
            Toast.makeText(getContext(), "Quiz completed! Score: " + correctAnswers +
                    " out of " + quizQuestions.size(), Toast.LENGTH_LONG).show();
        }
    }
}