package com.example.learningapp;

import android.content.Context;
import android.content.SharedPreferences;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QuizFragment extends Fragment {

    private static final String TAG = "QuizFragment";
    private static final String API_BASE_URL = "http://10.0.2.2:5001";

    private TextView questionTextView;
    private RadioGroup optionsRadioGroup;
    private Button submitButton;
    private ProgressBar progressBar;
    private View loadingView;
    private View quizContent;

    private int currentQuestionIndex = 0;
    private List<QuizQuestion> quizQuestions = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>(); // Track user answers
    private int correctAnswers = 0;
    private String currentTopic = "movies"; // Default topic

    // QuizQuestion inner class
    private static class QuizQuestion {
        private final String question;
        private final List<String> options;
        private final String answer;
        private final String correctAnswerText;

        public QuizQuestion(String question, List<String> options, String answer, String correctAnswerText) {
            this.question = question;
            this.options = options;
            this.answer = answer;
            this.correctAnswerText = correctAnswerText;
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

        public String getCorrectAnswerText() {
            return correctAnswerText;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "=== QUIZ FRAGMENT STARTED ===");

        // Get topic from arguments
        Bundle args = getArguments();
        if (args != null) {
            currentTopic = args.getString("topic", "movies");
        }
        Log.d(TAG, "Quiz topic set to: " + currentTopic);

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

        // Test quiz save functionality
        testQuizSave();

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
        String apiUrl = API_BASE_URL + "/getQuiz?topic=" + currentTopic;

        Log.d(TAG, "Fetching quiz from: " + apiUrl);

        Request request = new Request.Builder()
                .url(apiUrl)
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
                Log.d(TAG, "Quiz API Response: " + responseData);

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
            userAnswers.clear();

            // Check if the response has the expected structure
            if (jsonResponse.has("quiz")) {
                JSONArray quizArray = jsonResponse.getJSONArray("quiz");
                Log.d(TAG, "Found " + quizArray.length() + " questions in quiz");

                // Parse each question
                for (int i = 0; i < quizArray.length(); i++) {
                    JSONObject questionObj = quizArray.getJSONObject(i);

                    // Get question, options, and answer - handle different field names
                    String question = questionObj.getString("question");

                    // Check both "answer" and "correct_answer" fields for letter
                    String answerLetter = "A"; // Default value
                    if (questionObj.has("answer")) {
                        answerLetter = questionObj.getString("answer");
                    }

                    // Get correct answer text
                    String correctAnswerText = "";
                    if (questionObj.has("correct_answer")) {
                        correctAnswerText = questionObj.getString("correct_answer");
                    }

                    JSONArray optionsArray = questionObj.getJSONArray("options");

                    List<String> options = new ArrayList<>();
                    for (int j = 0; j < optionsArray.length(); j++) {
                        options.add(optionsArray.getString(j));
                    }

                    // If no correct_answer text, use the option text based on letter
                    if (correctAnswerText.isEmpty() && !answerLetter.isEmpty()) {
                        int answerIndex = "ABCD".indexOf(answerLetter);
                        if (answerIndex >= 0 && answerIndex < options.size()) {
                            correctAnswerText = options.get(answerIndex);
                        }
                    }

                    Log.d(TAG, "Question " + (i+1) + ": " + question);
                    Log.d(TAG, "Answer letter: " + answerLetter + ", Answer text: " + correctAnswerText);

                    quizQuestions.add(new QuizQuestion(question, options, answerLetter, correctAnswerText));
                    userAnswers.add(""); // Initialize user answer as empty
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

            // Get the selected answer text
            RadioButton selectedRadioButton = (RadioButton) radioButton;
            String selectedAnswerText = selectedRadioButton.getText().toString();

            Log.d(TAG, "User selected: " + selectedAnswerText + " (index: " + radioIndex + ")");

            // Store user answer
            userAnswers.set(currentQuestionIndex, selectedAnswerText);

            // Check if answer is correct
            QuizQuestion currentQuestion = quizQuestions.get(currentQuestionIndex);
            String correctAnswerLetter = currentQuestion.getAnswer();
            int correctIndex = "ABCD".indexOf(correctAnswerLetter);

            boolean isCorrect = radioIndex == correctIndex;
            if (isCorrect) {
                correctAnswers++;
            }

            Log.d(TAG, "Question " + (currentQuestionIndex + 1) + " - Correct: " + isCorrect +
                    " (Selected: " + radioIndex + ", Correct: " + correctIndex + ")");

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
                // Quiz finished - save results and navigate
                Log.d(TAG, "Quiz completed! Final score: " + correctAnswers + "/" + quizQuestions.size());
                completeQuiz();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in submit answer: " + e.getMessage());
            Toast.makeText(getContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void completeQuiz() {
        Log.d(TAG, "=== QUIZ COMPLETED ===");
        Log.d(TAG, "About to save quiz result...");

        // Save quiz result to backend
        saveQuizResult();

        Log.d(TAG, "Save method called, now navigating...");

        // Show completion message
        Toast.makeText(getContext(), "Quiz completed! Score: " + correctAnswers + "/" + quizQuestions.size(),
                Toast.LENGTH_LONG).show();

        // Navigate to results or back to dashboard
        try {
            showQuizResults();
        } catch (Exception e) {
            Log.e(TAG, "Failed to navigate to results: " + e.getMessage());
            try {
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_quizFragment_to_dashboardFragment);
            } catch (Exception e2) {
                Log.e(TAG, "Failed to navigate back: " + e2.getMessage());
            }
        }
    }

    private void saveQuizResult() {
        Log.d(TAG, "=== STARTING QUIZ SAVE DEBUG ===");
        Log.d(TAG, "Current topic: " + currentTopic);
        Log.d(TAG, "Correct answers: " + correctAnswers);
        Log.d(TAG, "Total questions: " + quizQuestions.size());
        Log.d(TAG, "User answers size: " + userAnswers.size());
        Log.d(TAG, "API URL: " + API_BASE_URL + "/saveQuizResult");

        // Debug user answers
        for (int i = 0; i < userAnswers.size(); i++) {
            Log.d(TAG, "User answer " + i + ": " + userAnswers.get(i));
        }

        try {
            // Prepare quiz result data
            JSONObject resultData = new JSONObject();
            resultData.put("username", getUsernameFromPrefs());
            resultData.put("topic", currentTopic);
            resultData.put("score", correctAnswers);
            resultData.put("total_questions", quizQuestions.size());

            // Prepare questions data
            JSONArray questionsArray = new JSONArray();
            for (int i = 0; i < quizQuestions.size(); i++) {
                QuizQuestion question = quizQuestions.get(i);
                JSONObject questionData = new JSONObject();
                questionData.put("question", question.getQuestion());
                questionData.put("user_answer", userAnswers.get(i));
                questionData.put("correct_answer", question.getCorrectAnswerText());
                questionData.put("is_correct", userAnswers.get(i).equals(question.getCorrectAnswerText()));
                questionsArray.put(questionData);
            }
            resultData.put("questions", questionsArray);

            Log.d(TAG, "Saving quiz result JSON: " + resultData.toString());

            // Send to backend
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, resultData.toString());

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/saveQuizResult")
                    .post(body)
                    .build();

            Log.d(TAG, "Sending POST request to: " + API_BASE_URL + "/saveQuizResult");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "QUIZ SAVE FAILED", e);
                    Log.e(TAG, "Failed URL: " + API_BASE_URL + "/saveQuizResult");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    if (response.isSuccessful()) {
                        Log.d(TAG, "QUIZ SAVE SUCCESS - Response: " + responseBody);
                    } else {
                        Log.e(TAG, "QUIZ SAVE FAILED - Code: " + response.code() + ", Response: " + responseBody);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "QUIZ SAVE ERROR", e);
        }
    }

    // Test method to verify quiz saving works
    private void testQuizSave() {
        Log.d(TAG, "=== TESTING QUIZ SAVE ===");

        try {
            JSONObject testData = new JSONObject();
            testData.put("username", "student");
            testData.put("topic", "test");
            testData.put("score", 1);
            testData.put("total_questions", 1);
            testData.put("questions", new JSONArray());

            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, testData.toString());

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/saveQuizResult")
                    .post(body)
                    .build();

            Log.d(TAG, "Sending test save request...");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "TEST SAVE FAILED", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "TEST SAVE RESPONSE - Code: " + response.code() + ", Body: " + responseBody);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "TEST SAVE ERROR", e);
        }
    }

    private void showQuizResults() {
        // Create bundle with results data
        Bundle args = new Bundle();
        args.putInt("correctAnswers", correctAnswers);
        args.putInt("totalQuestions", quizQuestions.size());
        args.putString("topic", currentTopic);

        // Navigate to results fragment
        try {
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_quizFragment_to_quizResultsFragment, args);
        } catch (Exception e) {
            Log.e(TAG, "Navigation failed: " + e.getMessage());
            // Fall back to dashboard navigation
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_quizFragment_to_dashboardFragment);
        }
    }

    private String getUsernameFromPrefs() {
        try {
            SharedPreferences prefs = requireActivity().getSharedPreferences(
                    "UserPrefs", Context.MODE_PRIVATE);
            String username = prefs.getString("username", "student");
            Log.d(TAG, "Got username from prefs: " + username);
            return username;
        } catch (Exception e) {
            Log.e(TAG, "Error getting username from prefs", e);
            return "student";
        }
    }
}