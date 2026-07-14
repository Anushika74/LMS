package com.ati.lms.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityProfileBinding;
import com.ati.lms.models.User;
import com.ati.lms.utils.FileUtils;
import com.ati.lms.utils.SessionManager;
import com.ati.lms.utils.ValidationUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

/**
 * ProfileActivity - View and edit user profile
 * Supports profile image change for both students and lecturers
 */
public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private User currentUser;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        binding.ivProfile.setImageURI(selectedImageUri);
                        // Save image
                        String imagePath = FileUtils.copyFileToAppStorage(this,
                                selectedImageUri, FileUtils.getProfileImagesFolder());
                        if (imagePath != null) {
                            dbHelper.updateProfileImage(currentUser.getId(), imagePath);
                            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnChangePhoto.setOnClickListener(v -> selectImage());
        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.btnLogout.setOnClickListener(v -> logout());

        setupDarkModeSwitch();
        loadProfile();
    }

    private void setupDarkModeSwitch() {
        binding.switchDarkMode.setChecked(sessionManager.isDarkMode());
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void loadProfile() {
        currentUser = dbHelper.getUserById(sessionManager.getUserId());
        if (currentUser != null) {
            binding.etFullName.setText(currentUser.getFullName());
            binding.etEmail.setText(currentUser.getEmail());
            binding.tvHeaderName.setText(currentUser.getFullName());
            binding.tvRole.setText(capitalize(currentUser.getRole()));

            if (currentUser.getProfileImage() != null && !currentUser.getProfileImage().isEmpty()) {
                File imgFile = new File(currentUser.getProfileImage());
                if (imgFile.exists()) {
                    binding.ivProfile.setImageURI(Uri.fromFile(imgFile));
                }
            }

            // Show student ID or designation
            if (currentUser.isStudent() && currentUser.getStudentId() != null
                    && !currentUser.getStudentId().isEmpty()) {
                binding.tvExtraInfo.setText("Student ID: " + currentUser.getStudentId());
                binding.tvExtraInfo.setVisibility(android.view.View.VISIBLE);
            } else if (currentUser.isLecturer() && currentUser.getDesignation() != null
                    && !currentUser.getDesignation().isEmpty()) {
                binding.tvExtraInfo.setText(currentUser.getDesignation());
                binding.tvExtraInfo.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvExtraInfo.setVisibility(android.view.View.GONE);
            }
        }
    }

    /**
     * Capitalize the first letter of a string safely (handles null/empty).
     */
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveProfile() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        // Clear any previous errors before re-validating
        binding.tilFullName.setError(null);
        binding.tilEmail.setError(null);

        if (!ValidationUtils.isValidName(fullName)) {
            binding.tilFullName.setError("Please enter a valid name");
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.setError("Please enter a valid email");
            return;
        }

        // Check if email changed and is not taken by another user
        if (!email.equals(currentUser.getEmail()) && dbHelper.isEmailExists(email)) {
            binding.tilEmail.setError("This email is already in use");
            return;
        }

        boolean success = dbHelper.updateUserProfile(currentUser.getId(), fullName, email, null);
        if (success) {
            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            sessionManager.updateName(fullName);
            sessionManager.updateEmail(email);
            binding.tvHeaderName.setText(fullName);
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}