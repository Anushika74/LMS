package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLecturerRegistrationBinding;
import com.ati.lms.utils.ValidationUtils;

/**
 * LecturerRegistrationActivity - Handles new lecturer account creation
 */
public class LecturerRegistrationActivity extends AppCompatActivity {

    private ActivityLecturerRegistrationBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);

        setupDesignationDropdown();

        binding.btnRegister.setOnClickListener(v -> attemptRegistration());
        binding.tvLogin.setOnClickListener(v -> finish());
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupDesignationDropdown() {
        String[] designations = {"Senior Lecturer", "Visiting Lecturer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, designations);
        binding.actvDesignation.setAdapter(adapter);
    }

    private void attemptRegistration() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        String designation = binding.actvDesignation.getText().toString().trim();

        // Clear errors
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        binding.tilDesignation.setError(null);

        // Validate
        if (!ValidationUtils.isValidName(fullName)) {
            binding.tilFullName.setError("Please enter your full name");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Please enter a valid email address");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError("Password must be at least 6 characters with letters and numbers");
            return;
        }
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }
        if (!ValidationUtils.isNotEmpty(designation)) {
            binding.tilDesignation.setError("Please select a designation");
            return;
        }

        // Check duplicates
        if (dbHelper.isEmailExists(email)) {
            binding.tilEmail.setError("This email is already registered");
            return;
        }

        // Show progress
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        // Register
        long result = dbHelper.registerUser(fullName, email, password, "lecturer", null, designation);

        binding.progressBar.setVisibility(View.GONE);
        binding.btnRegister.setEnabled(true);

        if (result > 0) {
            Toast.makeText(this, "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}