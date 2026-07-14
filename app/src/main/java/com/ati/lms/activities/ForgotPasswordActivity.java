package com.ati.lms.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityForgotPasswordBinding;
import com.ati.lms.utils.ValidationUtils;

/**
 * ForgotPasswordActivity - Allows users to reset their password
 * Verifies email exists then allows setting new password
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private DatabaseHelper dbHelper;
    private boolean emailVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);

        // Initially hide password fields
        binding.layoutNewPassword.setVisibility(View.GONE);

        binding.btnVerifyEmail.setOnClickListener(v -> verifyEmail());
        binding.btnResetPassword.setOnClickListener(v -> resetPassword());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void verifyEmail() {
        String email = binding.etEmail.getText().toString().trim();
        binding.tilEmail.setError(null);

        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Please enter a valid email");
            return;
        }

        if (!dbHelper.isEmailExists(email)) {
            binding.tilEmail.setError("No account found with this email");
            return;
        }

        emailVerified = true;
        binding.layoutNewPassword.setVisibility(View.VISIBLE);
        binding.btnVerifyEmail.setVisibility(View.GONE);
        binding.etEmail.setEnabled(false);
        Toast.makeText(this, "Email verified! Set your new password.", Toast.LENGTH_SHORT).show();
    }

    private void resetPassword() {
        if (!emailVerified) return;

        String email = binding.etEmail.getText().toString().trim();
        String newPassword = binding.etNewPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        if (!ValidationUtils.isValidPassword(newPassword)) {
            binding.tilNewPassword.setError("Password must be at least 6 characters with letters and numbers");
            return;
        }
        if (!ValidationUtils.doPasswordsMatch(newPassword, confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        boolean success = dbHelper.updatePassword(email, newPassword);
        if (success) {
            Toast.makeText(this, "Password reset successfully! Please login.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to reset password. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}