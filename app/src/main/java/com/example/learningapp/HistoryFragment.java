package com.example.learningapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private static final String API_BASE_URL = "http://10.0.2.2:5001";

    private LinearLayout historyContainer;
    private View loadingView;
    private View contentView;
    private TextView emptyStateText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "HistoryFragment onViewCreated");

        // Setup back button
        setupBackButton();

        // Initialize views
        historyContainer = view.findViewById(R.id.historyContainer);
        loadingView = view.findViewById(R.id.loadingView);
        contentView = view.findViewById(R.id.contentView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Load quiz history
        loadQuizHistory();
    }

    private void setupBackButton() {
        ImageButton backButton = getView().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(requireView()).navigateUp();
                } catch (Exception e) {
                    Log.e(TAG, "Back navigation failed: " + e.getMessage());
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                }
            });
            Log.d(TAG, "Back button click listener set");
        } else {
            Log.e(TAG, "Back button not found");
        }
    }

    private void loadQuizHistory() {
        showLoading(true);

        OkHttpClient client = new OkHttpClient();
        String username = getUsernameFromPrefs();
        String apiUrl = API_BASE_URL + "/getQuizHistory?username=" + username;

        Log.d(TAG, "Loading quiz history from: " + apiUrl);

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to load history", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        showEmptyState("Failed to load history. Please try again.");
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "History response: " + responseData);
                    try {
                        JSONObject historyData = new JSONObject(responseData);
                        JSONArray historyArray = historyData.getJSONArray("history");

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                if (historyArray.length() > 0) {
                                    populateHistory(historyArray);
                                } else {
                                    showEmptyState("No quiz history yet. Start taking quizzes to see your progress!");
                                }
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing history data", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                showEmptyState("Error loading history data.");
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "History request failed with code: " + response.code());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showEmptyState("No quiz history found.");
                        });
                    }
                }
            }
        });
    }

    private void populateHistory(JSONArray historyArray) {
        if (historyContainer != null) {
            historyContainer.removeAllViews();

            for (int i = 0; i < historyArray.length(); i++) {
                try {
                    JSONObject quizData = historyArray.getJSONObject(i);
                    createHistoryCard(quizData, i + 1);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating history card", e);
                }
            }
        }
    }

    private void createHistoryCard(JSONObject quizData, int questionNumber) {
        try {
            String topic = quizData.optString("topic", "General");
            int score = quizData.optInt("score", 0);
            int totalQuestions = quizData.optInt("total_questions", 0);
            String date = quizData.optString("date", "Unknown date");
            JSONArray questions = quizData.optJSONArray("questions");

            // Create main card
            CardView mainCard = new CardView(getContext());
            LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            mainParams.setMargins(0, 0, 0, dpToPx(16));
            mainCard.setLayoutParams(mainParams);
            mainCard.setCardBackgroundColor(getResources().getColor(R.color.royal_blue, null));
            mainCard.setRadius(dpToPx(12));
            mainCard.setCardElevation(dpToPx(4));

            // Main card content
            LinearLayout mainContent = new LinearLayout(getContext());
            mainContent.setOrientation(LinearLayout.VERTICAL);
            mainContent.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

            // Header with question number and topic
            LinearLayout headerLayout = new LinearLayout(getContext());
            headerLayout.setOrientation(LinearLayout.HORIZONTAL);
            headerLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView questionTitle = new TextView(getContext());
            questionTitle.setText(questionNumber + ". " + topic + " Quiz");
            questionTitle.setTextColor(getResources().getColor(android.R.color.white, null));
            questionTitle.setTextSize(18);
            questionTitle.setTypeface(null, android.graphics.Typeface.BOLD);

            // Expand/Collapse button
            TextView expandButton = new TextView(getContext());
            expandButton.setText("▼");
            expandButton.setTextColor(getResources().getColor(R.color.teal_200, null));
            expandButton.setTextSize(16);
            expandButton.setBackgroundColor(getResources().getColor(R.color.teal_200, null));
            expandButton.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
            LinearLayout.LayoutParams expandParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            expandParams.setMarginStart(dpToPx(8));
            expandButton.setLayoutParams(expandParams);

            headerLayout.addView(questionTitle);
            headerLayout.addView(expandButton);

            // Score and date info
            TextView scoreInfo = new TextView(getContext());
            scoreInfo.setText("Score: " + score + "/" + totalQuestions + " • " + date);
            scoreInfo.setTextColor(getResources().getColor(android.R.color.white, null));
            scoreInfo.setTextSize(14);
            LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            scoreParams.setMargins(0, dpToPx(8), 0, 0);
            scoreInfo.setLayoutParams(scoreParams);

            // Expanded content (initially hidden)
            LinearLayout expandedContent = new LinearLayout(getContext());
            expandedContent.setOrientation(LinearLayout.VERTICAL);
            expandedContent.setVisibility(View.GONE);
            LinearLayout.LayoutParams expandedParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            expandedParams.setMargins(0, dpToPx(12), 0, 0);
            expandedContent.setLayoutParams(expandedParams);

            // Add questions to expanded content if available
            if (questions != null && questions.length() > 0) {
                for (int j = 0; j < questions.length(); j++) {
                    try {
                        JSONObject question = questions.getJSONObject(j);
                        createQuestionView(expandedContent, question, j + 1);
                    } catch (Exception e) {
                        Log.e(TAG, "Error creating question view", e);
                    }
                }
            } else {
                TextView noQuestionsText = new TextView(getContext());
                noQuestionsText.setText("Question details not available");
                noQuestionsText.setTextColor(getResources().getColor(android.R.color.white, null));
                noQuestionsText.setTextSize(12);
                noQuestionsText.setTypeface(null, android.graphics.Typeface.ITALIC);
                expandedContent.addView(noQuestionsText);
            }

            // Add click listener for expand/collapse
            mainCard.setOnClickListener(v -> {
                if (expandedContent.getVisibility() == View.GONE) {
                    expandedContent.setVisibility(View.VISIBLE);
                    expandButton.setText("▲");
                } else {
                    expandedContent.setVisibility(View.GONE);
                    expandButton.setText("▼");
                }
            });

            // Assemble the card
            mainContent.addView(headerLayout);
            mainContent.addView(scoreInfo);
            mainContent.addView(expandedContent);
            mainCard.addView(mainContent);

            // Add to container
            if (historyContainer != null) {
                historyContainer.addView(mainCard);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error creating history card", e);
        }
    }

    private void createQuestionView(LinearLayout parent, JSONObject questionData, int questionNum) {
        try {
            String questionText = questionData.optString("question", "Question not available");
            String userAnswer = questionData.optString("user_answer", "Not answered");
            String correctAnswer = questionData.optString("correct_answer", "Unknown");
            boolean isCorrect = userAnswer.equals(correctAnswer);

            // Question container
            LinearLayout questionContainer = new LinearLayout(getContext());
            questionContainer.setOrientation(LinearLayout.VERTICAL);
            questionContainer.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
            questionContainer.setBackgroundColor(getResources().getColor(R.color.sky_blue, null));
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            containerParams.setMargins(0, 0, 0, dpToPx(8));
            questionContainer.setLayoutParams(containerParams);

            // Question text
            TextView questionTextView = new TextView(getContext());
            questionTextView.setText(questionNum + ". " + questionText);
            questionTextView.setTextColor(getResources().getColor(android.R.color.black, null));
            questionTextView.setTextSize(14);
            questionTextView.setTypeface(null, android.graphics.Typeface.BOLD);

            // Answer status
            TextView answerStatus = new TextView(getContext());
            if (isCorrect) {
                answerStatus.setText("✓ Your Answer: " + userAnswer + " (Correct)");
                answerStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
            } else {
                answerStatus.setText("✗ Your Answer: " + userAnswer + " (Correct: " + correctAnswer + ")");
                answerStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
            }
            answerStatus.setTextSize(12);
            LinearLayout.LayoutParams answerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            answerParams.setMargins(0, dpToPx(4), 0, 0);
            answerStatus.setLayoutParams(answerParams);

            questionContainer.addView(questionTextView);
            questionContainer.addView(answerStatus);
            parent.addView(questionContainer);

        } catch (Exception e) {
            Log.e(TAG, "Error creating question view", e);
        }
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null && contentView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            contentView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        }
    }

    private void showEmptyState(String message) {
        if (emptyStateText != null && historyContainer != null) {
            emptyStateText.setText(message);
            emptyStateText.setVisibility(View.VISIBLE);
            historyContainer.setVisibility(View.GONE);
        }
    }

    private String getUsernameFromPrefs() {
        try {
            android.content.SharedPreferences prefs = requireActivity().getSharedPreferences(
                    "UserPrefs", android.content.Context.MODE_PRIVATE);
            String username = prefs.getString("username", "student");
            Log.d(TAG, "Got username from prefs: " + username);
            return username;
        } catch (Exception e) {
            Log.e(TAG, "Error getting username from prefs", e);
            return "student";
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}