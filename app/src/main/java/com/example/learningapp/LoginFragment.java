package com.example.learningapp;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        super(R.layout.fragment_login);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etUser = view.findViewById(R.id.etUsername);
        EditText etPass = view.findViewById(R.id.etPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvSignup = view.findViewById(R.id.tvSignupLink);

        btnLogin.setOnClickListener(v -> {
            String username = etUser.getText().toString().trim();
            String password = etPass.getText().toString();

            // 1. Username non-empty
            if (username.isEmpty()) {
                etUser.setError(getString(R.string.error_username_required));
                etUser.requestFocus();
                return;
            }

            // 2. Password validation: ≥6 chars, uppercase, lowercase, digit, special char
            String passwordPattern =
                    "^(?=.*[0-9])" +        // 1 digit
                            "(?=.*[a-z])" +         // 1 lowercase letter
                            "(?=.*[A-Z])" +         // 1 uppercase letter
                            "(?=.*[@#$%^&+=!])" +   // 1 special character
                            "(?=\\S+$)" +           // no white spaces
                            ".{6,}$";               // 6 characters

            if (!password.matches(passwordPattern)) {
                etPass.setError(getString(R.string.error_password_invalid));
                etPass.requestFocus();
                return;
            }


            Navigation.findNavController(v).navigate(R.id.action_login_to_dashboard);
        });

        tvSignup.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_signup)
        );
    }
}
