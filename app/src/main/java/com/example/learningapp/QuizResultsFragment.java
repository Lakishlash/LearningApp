package com.example.learningapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class QuizResultsFragment extends Fragment {

    private TextView resultsTitle;
    private TextView question1Result;
    private TextView question2Result;
    private TextView question3Result;
    private Button continueButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        resultsTitle = view.findViewById(R.id.resultsTitle);
        question1Result = view.findViewById(R.id.question1Result);
        question2Result = view.findViewById(R.id.question2Result);
        question3Result = view.findViewById(R.id.question3Result);
        continueButton = view.findViewById(R.id.continueButton);

        // Get arguments
        Bundle args = getArguments();
        if (args != null) {
            int correctAnswers = args.getInt("correctAnswers", 0);
            int totalQuestions = args.getInt("totalQuestions", 0);

            // Set the results text
            String resultText = String.format("You scored %d out of %d", correctAnswers, totalQuestions);
            resultsTitle.setText("Your Results");

            // Example model responses
            question1Result.setText("1. Question 1\nResponse from the model based on your answer.");
            question2Result.setText("2. Question 2\nResponse from the model based on your answer.");
            question3Result.setText("3. Question 3\nResponse from the model based on your answer.");
        }

        // Set continue button click listener
        continueButton.setOnClickListener(v -> {
            // Navigate back to dashboard
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_quizResultsFragment_to_dashboardFragment);
        });
    }
}