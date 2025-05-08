package com.example.learningapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class SignupFragment extends Fragment {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText confirmEmailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText phoneEditText;
    private Button createAccountButton;

    public SignupFragment() {
        super(R.layout.fragment_signup);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        usernameEditText = view.findViewById(R.id.usernameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        confirmEmailEditText = view.findViewById(R.id.confirmEmailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        createAccountButton = view.findViewById(R.id.createAccountButton);

        // Set click listener for signup button
        createAccountButton.setOnClickListener(v -> attemptSignup());
    }

    private void attemptSignup() {
        // Get input values
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String confirmEmail = confirmEmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validate input
        if (username.isEmpty() || email.isEmpty() || confirmEmail.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.equals(confirmEmail)) {
            Toast.makeText(getContext(), "Email addresses do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password pattern validation
        String passwordPattern =
                "^(?=.*[0-9])" +        // at least 1 digit
                        "(?=.*[a-z])" +         // at least 1 lowercase letter
                        "(?=.*[A-Z])" +         // at least 1 uppercase letter
                        "(?=.*[@#$%^&+=!])" +   // at least 1 special character
                        "(?=\\S+$)" +           // no white spaces
                        ".{6,}$";               // at least 6 characters

        if (!password.matches(passwordPattern)) {
            Toast.makeText(getContext(), "Password must include at least 6 characters with uppercase, lowercase, number & special character", Toast.LENGTH_LONG).show();
            return;
        }

        signupSuccessful(username, email);
    }

    private void signupSuccessful(String username, String email) {
        // Save user info
        SharedPreferences prefs = requireActivity().getSharedPreferences(
                "UserPrefs", Context.MODE_PRIVATE);

        prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("username", username)
                .putString("email", email)
                .apply();

        // Show success toast message
        Toast.makeText(getContext(), "Account created successfully!", Toast.LENGTH_SHORT).show();

        // Navigate to interests selection
        Navigation.findNavController(requireView())
                .navigate(R.id.action_signup_to_interests);
    }
}