package com.example.learningapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterestsFragment extends Fragment {

    private ChipGroup interestsChipGroup;
    private Button nextButton;
    private final List<String> selectedInterests = new ArrayList<>();
    private static final int MAX_INTERESTS = 10;

    // Available interest topics
    private final String[] availableTopics = {
            "Algorithms", "Data Structures", "Web Development", "Testing",
            "Mobile Apps", "Machine Learning", "Cloud Computing", "Databases",
            "UI/UX Design", "DevOps", "Cybersecurity", "Game Development",
            "Blockchain", "IoT", "Computer Networks", "Operating Systems"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        interestsChipGroup = view.findViewById(R.id.interestsChipGroup);
        nextButton = view.findViewById(R.id.nextButton);

        // Create chips for all available topics
        setupInterestChips();

        // Set click listener for next button
        nextButton.setOnClickListener(v -> {
            if (selectedInterests.isEmpty()) {
                Toast.makeText(getContext(), "Please select at least one interest", Toast.LENGTH_SHORT).show();
            } else {
                // Save selected interests to SharedPreferences
                saveInterests();

                // Navigate to dashboard
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_interestsFragment_to_dashboardFragment);
            }
        });
    }

    private void setupInterestChips() {
        // Clear any existing chips
        interestsChipGroup.removeAllViews();

        // Add chips for each available topic
        for (String topic : availableTopics) {
            Chip chip = new Chip(requireContext());
            chip.setText(topic);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector, null));

            // Set chip click listener
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Add to selected list if not already at max
                    if (selectedInterests.size() < MAX_INTERESTS) {
                        selectedInterests.add(topic);
                    } else {
                        // Uncheck the chip if already at max
                        chip.setChecked(false);
                        Toast.makeText(getContext(),
                                "You can select up to " + MAX_INTERESTS + " topics",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Remove from selected list
                    selectedInterests.remove(topic);
                }
            });

            interestsChipGroup.addView(chip);
        }
    }

    private void saveInterests() {
        // Get SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences(
                "UserPrefs", Context.MODE_PRIVATE);

        // Convert list to set for storage
        Set<String> interestSet = new HashSet<>(selectedInterests);

        // Save interests
        prefs.edit().putStringSet("user_interests", interestSet).apply();

        // Log the saved interests
        StringBuilder sb = new StringBuilder("Saved interests: ");
        for (String interest : selectedInterests) {
            sb.append(interest).append(", ");
        }
        String logMessage = sb.toString();
        if (logMessage.endsWith(", ")) {
            logMessage = logMessage.substring(0, logMessage.length() - 2);
        }

        Toast.makeText(getContext(), "Interests saved!", Toast.LENGTH_SHORT).show();
    }
}