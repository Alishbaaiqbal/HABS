package com.example.habs_mainpage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PayFastWebViewActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;

    // URLs must exactly match those in PaymentMethodActivity
    private static final String RETURN_URL = "https://yourdomain.com/success";
    private static final String CANCEL_URL = "https://yourdomain.com/cancel";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payfastwebview);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        // Enable JavaScript (required for payment page)
        webView.getSettings().setJavaScriptEnabled(true);

        // Set the custom WebViewClient to handle URL loading and progress
        webView.setWebViewClient(new PayFastWebViewClient());

        String paymentUrl = getIntent().getStringExtra("paymentUrl");

        if (paymentUrl != null && !paymentUrl.isEmpty()) {
            webView.loadUrl(paymentUrl);
        } else {
            // If no payment URL, cancel and finish
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }

    private class PayFastWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(ProgressBar.GONE);
            super.onPageFinished(view, url);
        }

        // For API 24+
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            return handleUrl(url);
        }

        // For older APIs
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUrl(url);
        }

        private boolean handleUrl(String url) {
            if (url.startsWith(RETURN_URL)) {
                // Payment successful
                setResult(Activity.RESULT_OK);
                finish();
                return true;
            } else if (url.startsWith(CANCEL_URL)) {
                // Payment cancelled
                setResult(Activity.RESULT_CANCELED);
                finish();
                return true;
            }
            // Allow WebView to load other URLs normally
            return false;
        }
    }
}
