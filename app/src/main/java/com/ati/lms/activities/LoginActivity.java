package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLoginBinding;
import com.ati.lms.models.User;
import com.ati.lms.utils.SessionManager;
import com.ati.lms.utils.ValidationUtils;
import com.google.android.material.tabs.TabLayout;

/**
 * LoginActivity - Handles user authentication
 * Supports email/password login with role selection
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private boolean isStudent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        setupTabs();
        setupClickListeners();
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isStudent = tab.getPosition() == 0;
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegistrationActivity.class)));
        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Please enter a valid email");
            return;
        }
        if (!ValidationUtils.isNotEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnLogin.setEnabled(false);

        // Attempt login
        User user = dbHelper.loginUser(email, password);

        binding.progressBar.setVisibility(View.GONE);
        binding.btnLogin.setEnabled(true);

        if (user != null) {
            // Check if selected role matches user's role
            String selectedRole = isStudent ? "student" : "lecturer";
            if (!user.getRole().equals(selectedRole)) {
                Toast.makeText(this, "Please select the correct role (Student/Lecturer)", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean rememberMe = binding.cbRememberMe.isChecked();
            sessionManager.createLoginSession(
                    user.getId(), user.getFullName(), user.getEmail(), user.getRole(), rememberMe);

            Intent intent;
            if (user.isLecturer()) {
                intent = new Intent(this, LecturerDashboardActivity.class);
            } else {
                intent = new Intent(this, StudentDashboardActivity.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            binding.tilPassword.setError("Invalid credentials");
        }
    }
}