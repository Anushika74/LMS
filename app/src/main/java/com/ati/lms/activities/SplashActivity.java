package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ati.lms.R;
import com.ati.lms.utils.SessionManager;

/**
 * SplashActivity - App launch screen with auto-navigation
 * Checks session state and redirects accordingly
 */
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager session = new SessionManager(this);
        if (session.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (session.isLoggedIn()) {
                // Navigate based on role
                String role = session.getUserRole();
                Intent intent;
                if ("lecturer".equals(role)) {
                    intent = new Intent(this, LecturerDashboardActivity.class);
                } else {
                    intent = new Intent(this, StudentDashboardActivity.class);
                }
                startActivity(intent);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}