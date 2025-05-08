package com.example.learningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView welcomeTextView;
    private TextView taskCountTextView;
    private MaterialCardView task1Card;
    private MaterialCardView task2Card;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        taskCountTextView = view.findViewById(R.id.taskCountTextView);
        task1Card = view.findViewById(R.id.task1Card);
        task2Card = view.findViewById(R.id.task2Card);

        // Set user's name
        String username = getUsernameFromPrefs();
        welcomeTextView.setText(String.format("Hello, %s!", username));

        // Set task count
        int taskCount = 2; // Hardcoded
        taskCountTextView.setText(String.format(Locale.getDefault(), "You have %d tasks due", taskCount));

        // Set click listeners for task cards
        task1Card.setOnClickListener(v -> {
            // Navigate to the quiz fragment
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_dashboardFragment_to_quizFragment);
        });

        task2Card.setOnClickListener(v -> {
            // navigate to a different type of task here
            // also go to quiz with a different parameter
            Bundle args = new Bundle();
            args.putString("topic", "science");
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_dashboardFragment_to_quizFragment, args);
        });
    }

    private String getUsernameFromPrefs() {

        // return hardcoded value
        return "Student";
    }
}