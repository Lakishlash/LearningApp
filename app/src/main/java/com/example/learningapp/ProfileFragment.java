package com.example.learningapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private static final String API_BASE_URL = "http://10.0.2.2:5001";

    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView totalQuestionsTextView;
    private TextView correctAnswersTextView;
    private TextView incorrectAnswersTextView;
    private TextView notificationTextView;
    private Button shareButton;
    private Button historyButtonVisible;
    private Button upgradeButton;
    private Button historyButton;
    private View loadingView;
    private View contentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "ProfileFragment onViewCreated");

        // Initialize views
        initializeViews(view);

        // Setup back button
        setupBackButton();

        // Load user data
        loadUserProfile();
        loadUserStats();

        // Set click listeners
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to profile
        Log.d(TAG, "onResume - refreshing profile data");
        loadUserProfile();
        loadUserStats();
    }

    private void initializeViews(View view) {
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        totalQuestionsTextView = view.findViewById(R.id.totalQuestionsTextView);
        correctAnswersTextView = view.findViewById(R.id.correctAnswersTextView);
        incorrectAnswersTextView = view.findViewById(R.id.incorrectAnswersTextView);
        notificationTextView = view.findViewById(R.id.notificationTextView);
        shareButton = view.findViewById(R.id.shareButton);
        historyButtonVisible = view.findViewById(R.id.historyButtonVisible);
        upgradeButton = view.findViewById(R.id.upgradeButton);
        historyButton = view.findViewById(R.id.historyButton);
        loadingView = view.findViewById(R.id.loadingView);
        contentView = view.findViewById(R.id.contentView);

        Log.d(TAG, "Views initialized. historyButtonVisible found: " + (historyButtonVisible != null));
        Log.d(TAG, "Views initialized. shareButton found: " + (shareButton != null));
        Log.d(TAG, "Views initialized. upgradeButton found: " + (upgradeButton != null));
    }

    private void setupBackButton() {
        ImageButton backButton = getView().findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(requireView()).navigateUp();
                } catch (Exception e) {
                    Log.e(TAG, "Back navigation failed: " + e.getMessage());
                    // Fallback - go to dashboard
                    try {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_dashboardFragment_to_profileFragment);
                    } catch (Exception e2) {
                        Log.e(TAG, "Fallback navigation failed: " + e2.getMessage());
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    }
                }
            });
            Log.d(TAG, "Back button click listener set");
        } else {
            Log.e(TAG, "Back button not found");
        }
    }

    private void setupClickListeners() {
        // Share button - Navigate to ShareFragment
        if (shareButton != null) {
            shareButton.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Share button clicked - navigating to share fragment");
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_profileFragment_to_shareFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Share navigation failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            Log.d(TAG, "Share button click listener set");
        } else {
            Log.e(TAG, "shareButton is null - button not found in layout");
        }

        // Main History Button - Simple and Direct
        if (historyButtonVisible != null) {
            historyButtonVisible.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "History button clicked - attempting navigation");
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_profileFragment_to_historyFragment);
                } catch (Exception e) {
                    Log.e(TAG, "History navigation failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            Log.d(TAG, "History button click listener set");
        } else {
            Log.e(TAG, "historyButtonVisible is null - button not found in layout");
        }

        // Hidden history button (fallback)
        if (historyButton != null) {
            historyButton.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_profileFragment_to_historyFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Hidden history navigation failed: " + e.getMessage());
                }
            });
        }

        // Upgrade button - Make sure this works
        if (upgradeButton != null) {
            upgradeButton.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Upgrade button clicked - navigating to upgrade fragment");
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_profileFragment_to_upgradeFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Upgrade navigation failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
            Log.d(TAG, "Upgrade button click listener set");
        } else {
            Log.e(TAG, "upgradeButton is null - button not found in layout");
        }

        // Handle "Summarized by AI" button click
        View summarizedSection = getView().findViewById(R.id.summarizedSection);
        if (summarizedSection != null) {
            summarizedSection.setOnClickListener(v -> {
                Log.d(TAG, "AI Summary section clicked");
                // Call AI Summary API
                generateAISummary();
            });
            Log.d(TAG, "AI Summary section click listener set");
        }

        // Alternative: Make the entire incorrect answers card clickable for history
        View incorrectAnswersCard = getView().findViewById(R.id.incorrectAnswersCard);
        if (incorrectAnswersCard != null) {
            incorrectAnswersCard.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Incorrect answers card clicked - navigating to history");
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_profileFragment_to_historyFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Card navigation failed: " + e.getMessage());
                }
            });
            Log.d(TAG, "Incorrect answers card click listener set");
        }

        Log.d(TAG, "All click listeners set up");
    }

    private void loadUserProfile() {
        showLoading(true);

        OkHttpClient client = new OkHttpClient();
        String username = getUsernameFromPrefs();
        String apiUrl = API_BASE_URL + "/getUserProfile?username=" + username;

        Log.d(TAG, "Loading profile from: " + apiUrl);

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to load profile", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        // Set default values if API fails
                        setDefaultProfileData();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Profile response: " + responseData);
                    try {
                        JSONObject profileData = new JSONObject(responseData);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateProfileUI(profileData);
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing profile data", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> setDefaultProfileData());
                        }
                    }
                } else {
                    Log.e(TAG, "Profile request failed with code: " + response.code());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> setDefaultProfileData());
                    }
                }
            }
        });
    }

    private void loadUserStats() {
        OkHttpClient client = new OkHttpClient();
        String username = getUsernameFromPrefs();
        String apiUrl = API_BASE_URL + "/getUserStats?username=" + username;

        Log.d(TAG, "Loading stats from: " + apiUrl);

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to load stats", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        setDefaultStatsData();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Stats response: " + responseData);
                    try {
                        JSONObject statsData = new JSONObject(responseData);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateStatsUI(statsData);
                                showLoading(false);
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing stats data", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                setDefaultStatsData();
                                showLoading(false);
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "Stats request failed with code: " + response.code());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            setDefaultStatsData();
                            showLoading(false);
                        });
                    }
                }
            }
        });
    }

    private void generateAISummary() {
        Log.d(TAG, "Generating AI Summary");
        OkHttpClient client = new OkHttpClient();
        String username = getUsernameFromPrefs();

        try {
            JSONObject requestData = new JSONObject();
            requestData.put("username", username);

            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, requestData.toString());

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/generateAISummary")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "AI Summary request failed", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Failed to get AI summary", Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject summaryData = new JSONObject(responseData);
                            String summary = summaryData.optString("summary", "No summary available");

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Log.d(TAG, "AI Summary received: " + summary);
                                    Toast.makeText(getContext(), "AI Summary: " + summary, Toast.LENGTH_LONG).show();
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing AI summary", e);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getContext(), "Error parsing AI summary", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    } else {
                        Log.e(TAG, "AI Summary request failed with code: " + response.code());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "AI summary not available", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating AI summary request", e);
            Toast.makeText(getContext(), "Error requesting AI summary", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProfileUI(JSONObject profileData) {
        try {
            String username = profileData.optString("username", "Student");
            String email = profileData.optString("email", "student@example.com");

            Log.d(TAG, "Updating profile UI - Username: " + username + ", Email: " + email);

            usernameTextView.setText(username);
            emailTextView.setText(email);
        } catch (Exception e) {
            Log.e(TAG, "Error updating profile UI", e);
            setDefaultProfileData();
        }
    }

    private void updateStatsUI(JSONObject statsData) {
        try {
            int totalQuestions = statsData.optInt("total_questions", 0);
            int correctAnswers = statsData.optInt("correctly_answered", 0);
            int incorrectAnswers = statsData.optInt("incorrect_answers", 0);
            int totalQuizzes = statsData.optInt("total_quizzes", 0);

            Log.d(TAG, "Updating stats UI - Total: " + totalQuestions +
                    ", Correct: " + correctAnswers +
                    ", Incorrect: " + incorrectAnswers +
                    ", Quizzes: " + totalQuizzes);

            totalQuestionsTextView.setText(String.valueOf(totalQuestions));
            correctAnswersTextView.setText(String.valueOf(correctAnswers));
            incorrectAnswersTextView.setText(String.valueOf(incorrectAnswers));

            // Update notification
            String notification;
            if (totalQuizzes == 0) {
                notification = "Start taking quizzes to see your progress!";
            } else {
                notification = "Great job! You've completed " + totalQuizzes + " quizzes";
            }
            notificationTextView.setText(notification);

        } catch (Exception e) {
            Log.e(TAG, "Error updating stats UI", e);
            setDefaultStatsData();
        }
    }

    private void setDefaultProfileData() {
        Log.d(TAG, "Setting default profile data");
        String username = getUsernameFromPrefs();
        usernameTextView.setText(username);
        emailTextView.setText(username.toLowerCase() + "@example.com");
    }

    private void setDefaultStatsData() {
        Log.d(TAG, "Setting default stats data");
        totalQuestionsTextView.setText("0");
        correctAnswersTextView.setText("0");
        incorrectAnswersTextView.setText("0");
        notificationTextView.setText("Start taking quizzes to see your progress!");
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null && contentView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            contentView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
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

    // Method to manually refresh data (can be called from other fragments)
    public void refreshData() {
        Log.d(TAG, "Manual refresh requested");
        loadUserProfile();
        loadUserStats();
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