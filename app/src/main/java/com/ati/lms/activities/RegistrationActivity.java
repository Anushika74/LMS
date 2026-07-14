package com.ati.lms.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityRegistrationBinding;
import com.ati.lms.utils.ValidationUtils;
import com.google.android.material.tabs.TabLayout;

/**
 * RegistrationActivity - Unified registration for both Students and Lecturers
 */
public class RegistrationActivity extends AppCompatActivity {

    private ActivityRegistrationBinding binding;
    private DatabaseHelper dbHelper;
    private boolean isStudent = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);

        setupTabs();
        setupDesignationDropdown();
        setupClickListeners();
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    isStudent = true;
                    binding.tilStudentId.setVisibility(View.VISIBLE);
                    binding.tilDesignation.setVisibility(View.GONE);
                } else {
                    isStudent = false;
                    binding.tilStudentId.setVisibility(View.GONE);
                    binding.tilDesignation.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupDesignationDropdown() {
        String[] designations = {"Senior Lecturer", "Visiting Lecturer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, designations);
        binding.actvDesignation.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        binding.btnRegister.setOnClickListener(v -> attemptRegistration());
    }

    private void attemptRegistration() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Clear errors
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
        binding.tilStudentId.setError(null);
        binding.tilDesignation.setError(null);

        // Common validations
        if (!ValidationUtils.isValidName(fullName)) {
            binding.tilFullName.setError("Please enter your full name");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Please enter a valid email address");
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }

        String studentId = null;
        String designation = null;
        String role;

        if (isStudent) {
            role = "student";
            studentId = binding.etStudentId.getText().toString().trim();
            if (!ValidationUtils.isValidStudentId(studentId)) {
                binding.tilStudentId.setError("Please enter a valid Student ID");
                return;
            }
            if (dbHelper.isStudentIdExists(studentId)) {
                binding.tilStudentId.setError("This Student ID is already registered");
                return;
            }
        } else {
            role = "lecturer";
            designation = binding.actvDesignation.getText().toString().trim();
            if (!ValidationUtils.isNotEmpty(designation)) {
                binding.tilDesignation.setError("Please select a designation");
                return;
            }
        }

        if (dbHelper.isEmailExists(email)) {
            binding.tilEmail.setError("This email is already registered");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnRegister.setEnabled(false);

        long result = dbHelper.registerUser(fullName, email, password, role, studentId, designation);

        binding.progressBar.setVisibility(View.GONE);
        binding.btnRegister.setEnabled(true);

        if (result > 0) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}