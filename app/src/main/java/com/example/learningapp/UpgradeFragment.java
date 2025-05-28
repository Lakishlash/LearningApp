package com.example.learningapp;

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

public class UpgradeFragment extends Fragment {

    private static final String TAG = "UpgradeFragment";
    private static final String API_BASE_URL = "http://10.0.2.2:5001";

    private Button starterPurchaseButton;
    private Button intermediatePurchaseButton;
    private Button advancedPurchaseButton;
    private TextView currentTierTextView;
    private View loadingView;
    private View contentView;
    private View paymentBottomSheet;
    private Button googlePayButton;
    private Button applePayButton;
    private Button creditCardButton;

    private String selectedTier = "";
    private String currentPaymentMethod = "google_pay";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upgrade, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "UpgradeFragment onViewCreated");

        // Initialize views
        initializeViews(view);

        // Setup back button
        setupBackButton();

        // Load current subscription info
        loadCurrentSubscription();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        starterPurchaseButton = view.findViewById(R.id.starterPurchaseButton);
        intermediatePurchaseButton = view.findViewById(R.id.intermediatePurchaseButton);
        advancedPurchaseButton = view.findViewById(R.id.advancedPurchaseButton);
        currentTierTextView = view.findViewById(R.id.currentTierTextView);
        loadingView = view.findViewById(R.id.loadingView);
        contentView = view.findViewById(R.id.contentView);
        paymentBottomSheet = view.findViewById(R.id.paymentBottomSheet);
        googlePayButton = view.findViewById(R.id.googlePayButton);
        applePayButton = view.findViewById(R.id.applePayButton);
        creditCardButton = view.findViewById(R.id.creditCardButton);

        Log.d(TAG, "Views initialized");
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

    private void setupClickListeners() {
        if (starterPurchaseButton != null) {
            starterPurchaseButton.setOnClickListener(v -> showPaymentOptions("starter"));
        }
        if (intermediatePurchaseButton != null) {
            intermediatePurchaseButton.setOnClickListener(v -> showPaymentOptions("intermediate"));
        }
        if (advancedPurchaseButton != null) {
            advancedPurchaseButton.setOnClickListener(v -> showPaymentOptions("advanced"));
        }

        if (googlePayButton != null) {
            googlePayButton.setOnClickListener(v -> {
                currentPaymentMethod = "google_pay";
                processPayment();
            });
        }

        if (applePayButton != null) {
            applePayButton.setOnClickListener(v -> {
                currentPaymentMethod = "apple_pay";
                processPayment();
            });
        }

        if (creditCardButton != null) {
            creditCardButton.setOnClickListener(v -> {
                currentPaymentMethod = "credit_card";
                processPayment();
            });
        }

        // Close payment sheet when clicking outside
        if (paymentBottomSheet != null) {
            paymentBottomSheet.setOnClickListener(v -> hidePaymentOptions());
        }

        Log.d(TAG, "Click listeners set up");
    }

    private void loadCurrentSubscription() {
        // For now, show free tier as default
        if (currentTierTextView != null) {
            currentTierTextView.setText("Current Plan: Free");
        }
        Log.d(TAG, "Current subscription loaded (default: Free)");
    }

    private void showPaymentOptions(String tier) {
        selectedTier = tier;
        Log.d(TAG, "Showing payment options for tier: " + tier);

        if (paymentBottomSheet != null) {
            paymentBottomSheet.setVisibility(View.VISIBLE);

            // Animate the bottom sheet up
            paymentBottomSheet.animate()
                    .translationY(0)
                    .setDuration(300)
                    .start();
        }
    }

    private void hidePaymentOptions() {
        Log.d(TAG, "Hiding payment options");

        if (paymentBottomSheet != null) {
            paymentBottomSheet.animate()
                    .translationY(paymentBottomSheet.getHeight())
                    .setDuration(300)
                    .withEndAction(() -> paymentBottomSheet.setVisibility(View.GONE))
                    .start();
        }
    }

    private void processPayment() {
        Log.d(TAG, "Processing payment for tier: " + selectedTier + " with method: " + currentPaymentMethod);

        hidePaymentOptions();
        showLoading(true);

        // Create payment request
        try {
            JSONObject paymentData = new JSONObject();
            paymentData.put("username", getUsernameFromPrefs());
            paymentData.put("subscription_tier", selectedTier);
            paymentData.put("payment_method", currentPaymentMethod);

            Log.d(TAG, "Payment data: " + paymentData.toString());

            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, paymentData.toString());

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/processPayment")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Payment request failed", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(getContext(), "Payment failed. Please try again.", Toast.LENGTH_LONG).show();
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        Log.d(TAG, "Payment response: " + responseData);
                        try {
                            JSONObject result = new JSONObject(responseData);
                            boolean success = result.optBoolean("success", false);
                            String message = result.optString("message", "Payment processed");

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showLoading(false);
                                    if (success) {
                                        Toast.makeText(getContext(), "Payment successful! " + message, Toast.LENGTH_LONG).show();
                                        updateCurrentTier(selectedTier);
                                    } else {
                                        Toast.makeText(getContext(), "Payment failed: " + message, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing payment response", e);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    showLoading(false);
                                    Toast.makeText(getContext(), "Payment error occurred", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    } else {
                        Log.e(TAG, "Payment request failed with code: " + response.code());
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                Toast.makeText(getContext(), "Payment server error", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating payment request", e);
            showLoading(false);
            Toast.makeText(getContext(), "Payment setup error", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrentTier(String tier) {
        String displayTier = tier.substring(0, 1).toUpperCase() + tier.substring(1);
        Log.d(TAG, "Updating current tier to: " + displayTier);

        if (currentTierTextView != null) {
            currentTierTextView.setText("Current Plan: " + displayTier);
        }

        // Update button states
        switch (tier) {
            case "starter":
                if (starterPurchaseButton != null) {
                    starterPurchaseButton.setText("Current Plan");
                    starterPurchaseButton.setEnabled(false);
                }
                break;
            case "intermediate":
                if (intermediatePurchaseButton != null) {
                    intermediatePurchaseButton.setText("Current Plan");
                    intermediatePurchaseButton.setEnabled(false);
                }
                break;
            case "advanced":
                if (advancedPurchaseButton != null) {
                    advancedPurchaseButton.setText("Current Plan");
                    advancedPurchaseButton.setEnabled(false);
                }
                break;
        }
    }

    private void showLoading(boolean isLoading) {
        if (loadingView != null && contentView != null) {
            loadingView.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            contentView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
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