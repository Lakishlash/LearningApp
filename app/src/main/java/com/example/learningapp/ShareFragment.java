package com.example.learningapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ShareFragment extends Fragment {

    private static final String TAG = "ShareFragment";
    private static final String API_BASE_URL = "http://10.0.2.2:5001";

    private TextView shareUrlTextView;
    private TextView shareTextTextView;
    private ImageView qrCodeImageView;
    private Button shareWhatsAppButton;
    private Button shareEmailButton;
    private Button shareTwitterButton;
    private Button copyLinkButton;
    private View loadingView;
    private View contentView;

    private String shareUrl = "";
    private String shareText = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "ShareFragment onViewCreated");

        // Initialize views
        initializeViews(view);

        // Setup back button
        setupBackButton();

        // Load share data
        loadShareData();

        // Set click listeners
        setupClickListeners();
    }

    private void initializeViews(View view) {
        shareUrlTextView = view.findViewById(R.id.shareUrlTextView);
        shareTextTextView = view.findViewById(R.id.shareTextTextView);
        qrCodeImageView = view.findViewById(R.id.qrCodeImageView);
        shareWhatsAppButton = view.findViewById(R.id.shareWhatsAppButton);
        shareEmailButton = view.findViewById(R.id.shareEmailButton);
        shareTwitterButton = view.findViewById(R.id.shareTwitterButton);
        copyLinkButton = view.findViewById(R.id.copyLinkButton);
        loadingView = view.findViewById(R.id.loadingView);
        contentView = view.findViewById(R.id.contentView);

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
        if (shareWhatsAppButton != null) {
            shareWhatsAppButton.setOnClickListener(v -> shareToWhatsApp());
        }
        if (shareEmailButton != null) {
            shareEmailButton.setOnClickListener(v -> shareToEmail());
        }
        if (shareTwitterButton != null) {
            shareTwitterButton.setOnClickListener(v -> shareToTwitter());
        }
        if (copyLinkButton != null) {
            copyLinkButton.setOnClickListener(v -> copyLinkToClipboard());
        }

        Log.d(TAG, "Share click listeners set up");
    }

    private void loadShareData() {
        showLoading(true);

        OkHttpClient client = new OkHttpClient();
        String username = getUsernameFromPrefs();
        String apiUrl = API_BASE_URL + "/generateShareData?username=" + username;

        Log.d(TAG, "Loading share data from: " + apiUrl);

        Request request = new Request.Builder()
                .url(apiUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to load share data", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        setDefaultShareData();
                        Toast.makeText(getContext(), "Failed to load share data, using defaults", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Share data response: " + responseData);
                    try {
                        JSONObject shareData = new JSONObject(responseData);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                updateShareUI(shareData);
                                showLoading(false);
                            });
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing share data", e);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                showLoading(false);
                                setDefaultShareData();
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "Share data request failed with code: " + response.code());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            showLoading(false);
                            setDefaultShareData();
                        });
                    }
                }
            }
        });
    }

    private void updateShareUI(JSONObject shareData) {
        try {
            shareUrl = shareData.optString("share_url", "https://learningapp.com/profile/student");
            shareText = shareData.optString("share_text", "Check out my learning progress!");

            Log.d(TAG, "Updating share UI - URL: " + shareUrl + ", Text: " + shareText);

            if (shareUrlTextView != null) {
                shareUrlTextView.setText(shareUrl);
            }
            if (shareTextTextView != null) {
                shareTextTextView.setText(shareText);
            }

            // Generate QR code
            generateQRCode(shareUrl);

        } catch (Exception e) {
            Log.e(TAG, "Error updating share UI", e);
            setDefaultShareData();
        }
    }

    private void setDefaultShareData() {
        String username = getUsernameFromPrefs();
        shareUrl = "https://learningapp.com/profile/" + username;
        shareText = "Check out my learning progress on Learning App!";

        Log.d(TAG, "Setting default share data");

        if (shareUrlTextView != null) {
            shareUrlTextView.setText(shareUrl);
        }
        if (shareTextTextView != null) {
            shareTextTextView.setText(shareText);
        }

        generateQRCode(shareUrl);
    }

    private void generateQRCode(String text) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 256, 256);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            if (qrCodeImageView != null) {
                qrCodeImageView.setImageBitmap(bitmap);
            }
            Log.d(TAG, "QR code generated successfully");
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code", e);
            Toast.makeText(getContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToWhatsApp() {
        try {
            Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
            whatsappIntent.setType("text/plain");
            whatsappIntent.setPackage("com.whatsapp");
            whatsappIntent.putExtra(Intent.EXTRA_TEXT, shareText + "\n" + shareUrl);
            startActivity(whatsappIntent);
            Log.d(TAG, "WhatsApp share initiated");
        } catch (Exception e) {
            Log.e(TAG, "WhatsApp share failed", e);
            Toast.makeText(getContext(), "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "My Learning Progress");
            emailIntent.putExtra(Intent.EXTRA_TEXT, shareText + "\n\n" + shareUrl);
            startActivity(Intent.createChooser(emailIntent, "Share via Email"));
            Log.d(TAG, "Email share initiated");
        } catch (Exception e) {
            Log.e(TAG, "Email share failed", e);
            Toast.makeText(getContext(), "Email share failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareToTwitter() {
        try {
            Intent twitterIntent = new Intent(Intent.ACTION_SEND);
            twitterIntent.setType("text/plain");
            twitterIntent.setPackage("com.twitter.android");
            twitterIntent.putExtra(Intent.EXTRA_TEXT, shareText + " " + shareUrl);
            startActivity(twitterIntent);
            Log.d(TAG, "Twitter share initiated");
        } catch (Exception e) {
            Log.e(TAG, "Twitter share failed", e);
            Toast.makeText(getContext(), "Twitter not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyLinkToClipboard() {
        try {
            android.content.ClipboardManager clipboard =
                    (android.content.ClipboardManager) getActivity().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Share URL", shareUrl);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Link copied to clipboard");
        } catch (Exception e) {
            Log.e(TAG, "Copy to clipboard failed", e);
            Toast.makeText(getContext(), "Copy failed", Toast.LENGTH_SHORT).show();
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