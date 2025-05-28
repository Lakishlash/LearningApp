package com.example.learningapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";

    private TextView welcomeTextView;
    private TextView taskCountTextView;
    private MaterialCardView task1Card;
    private MaterialCardView task2Card;
    private androidx.cardview.widget.CardView profileCard;
    private View profileImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "DashboardFragment onViewCreated");

        // Initialize views
        initializeViews(view);

        // Set user data
        setupUserData();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        taskCountTextView = view.findViewById(R.id.taskCountTextView);
        task1Card = view.findViewById(R.id.task1Card);
        task2Card = view.findViewById(R.id.task2Card);
        profileImageView = view.findViewById(R.id.profileImageView);

        // Find the profile card view that contains the profile image
        if (profileImageView != null && profileImageView.getParent() instanceof androidx.cardview.widget.CardView) {
            profileCard = (androidx.cardview.widget.CardView) profileImageView.getParent();
        }

        Log.d(TAG, "Views initialized - profileImageView: " + (profileImageView != null));
    }

    private void setupUserData() {
        // Set user's name
        String username = getUsernameFromPrefs();
        welcomeTextView.setText(String.format("Hello, %s!", username));

        // Set task count
        int taskCount = 2; // Dynamic task count based on user's interests
        taskCountTextView.setText(String.format(Locale.getDefault(), "You have %d tasks due", taskCount));

        Log.d(TAG, "User data set up for: " + username);
    }

    private void setupClickListeners() {
        // Task 1 - Movies Quiz
        if (task1Card != null) {
            task1Card.setOnClickListener(v -> {
                Log.d(TAG, "Task 1 card clicked - Movies Quiz");
                // Navigate to the quiz fragment with movies topic
                Bundle args = new Bundle();
                args.putString("topic", "movies");
                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_dashboardFragment_to_quizFragment, args);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation to quiz failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Task 2 - Science Quiz
        if (task2Card != null) {
            task2Card.setOnClickListener(v -> {
                Log.d(TAG, "Task 2 card clicked - Science Quiz");
                // Navigate to quiz with science topic
                Bundle args = new Bundle();
                args.putString("topic", "science");
                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_dashboardFragment_to_quizFragment, args);
                } catch (Exception e) {
                    Log.e(TAG, "Navigation to quiz failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Profile navigation - Click on profile image
        if (profileImageView != null) {
            profileImageView.setOnClickListener(v -> {
                Log.d(TAG, "Profile image clicked - navigating to profile");
                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_dashboardFragment_to_profileFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Profile navigation failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Profile navigation error", Toast.LENGTH_SHORT).show();
                }
            });

            // Make it visually clickable
            profileImageView.setClickable(true);
            profileImageView.setFocusable(true);
            Log.d(TAG, "Profile image click listener set");
        } else {
            Log.e(TAG, "Profile image view is null");
        }

        // Profile card navigation (if card exists)
        if (profileCard != null) {
            profileCard.setOnClickListener(v -> {
                Log.d(TAG, "Profile card clicked - navigating to profile");
                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_dashboardFragment_to_profileFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Profile card navigation failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Profile navigation error", Toast.LENGTH_SHORT).show();
                }
            });

            // Make profile card visually indicate it's clickable
            profileCard.setClickable(true);
            profileCard.setFocusable(true);
            profileCard.setCardElevation(6.0f); // Slightly elevated to show it's interactive
            Log.d(TAG, "Profile card click listener set");
        }

        // Alternative: If profile card setup fails, add click to entire header section
        View headerSection = requireView().findViewById(R.id.welcomeTextView).getParent() instanceof View ?
                (View) requireView().findViewById(R.id.welcomeTextView).getParent() : null;

        if (headerSection != null && profileCard == null && profileImageView == null) {
            // Fallback: make the entire header clickable for profile navigation
            Log.d(TAG, "Setting up fallback header click listener");
            headerSection.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_dashboardFragment_to_profileFragment);
                } catch (Exception e) {
                    Log.e(TAG, "Fallback header navigation failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
                }
            });
        }

        Log.d(TAG, "All click listeners set up");
    }

    private String getUsernameFromPrefs() {
        try {
            // Get username from SharedPreferences
            SharedPreferences prefs = requireActivity().getSharedPreferences(
                    "UserPrefs", Context.MODE_PRIVATE);

            String username = prefs.getString("username", null);

            if (username != null && !username.isEmpty()) {
                Log.d(TAG, "Retrieved username from prefs: " + username);
                return username;
            }

            // Return default if no username found
            Log.d(TAG, "No username found in prefs, using default");
            return "Student";

        } catch (Exception e) {
            // Handle any errors gracefully
            Log.e(TAG, "Error getting username from prefs: " + e.getMessage());
            return "Student";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - refreshing user data");
        // Refresh user data when returning to dashboard
        setupUserData();
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