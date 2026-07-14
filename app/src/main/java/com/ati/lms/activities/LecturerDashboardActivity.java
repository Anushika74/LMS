package com.ati.lms.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.ati.lms.R;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityLecturerDashboardBinding;
import com.ati.lms.models.User;
import com.ati.lms.utils.SessionManager;

import java.io.File;

/**
 * LecturerDashboardActivity - Main home screen for lecturers
 * Shows statistics, quick actions, and recent uploads
 */
public class LecturerDashboardActivity extends AppCompatActivity {

    private ActivityLecturerDashboardBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLecturerDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        setupUI();
        setupQuickActions();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadStatistics();
    }

    private void loadProfile() {
        User user = dbHelper.getUserById(sessionManager.getUserId());
        if (user != null) {
            binding.tvGreeting.setText(getGreeting());
            binding.tvName.setText(user.getFullName());
            binding.tvDesignation.setText(user.getDesignation());
            binding.tvEmail.setText(user.getEmail());

            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                File imgFile = new File(user.getProfileImage());
                if (imgFile.exists()) {
                    binding.ivProfile.setImageURI(Uri.fromFile(imgFile));
                } else {
                    binding.ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
                }
            } else {
                binding.ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
            }
        }
    }

    private void setupUI() {
        // Any static UI setup if needed
    }

    private void loadStatistics() {
        int lecturerId = sessionManager.getUserId();
        binding.tvTotalCourses.setText(String.valueOf(dbHelper.getTotalCoursesCount()));
        binding.tvTotalModules.setText(String.valueOf(dbHelper.getTotalModulesCount()));
        binding.tvTotalNotes.setText(String.valueOf(dbHelper.getCountByLecturer("notes", lecturerId)));
        binding.tvTotalPastPapers.setText(String.valueOf(dbHelper.getCountByLecturer("past_papers", lecturerId)));
        binding.tvTotalAssignments.setText(String.valueOf(dbHelper.getCountByLecturer("assignments", lecturerId)));
        binding.tvTotalQuizzes.setText(String.valueOf(dbHelper.getCountByLecturer("quizzes", lecturerId)));
        binding.tvTotalNotices.setText(String.valueOf(dbHelper.getCountByLecturer("notices", lecturerId)));
    }

    private void setupQuickActions() {
        binding.cardUploadNotes.setOnClickListener(v ->
                startActivity(new Intent(this, CourseListActivity.class)));
        binding.cardUploadPastPapers.setOnClickListener(v ->
                startActivity(new Intent(this, CourseListActivity.class)));
        binding.cardCreateQuiz.setOnClickListener(v ->
                startActivity(new Intent(this, CourseListActivity.class)));
        binding.cardAddAssignment.setOnClickListener(v ->
                startActivity(new Intent(this, LecturerAssignmentsActivity.class)));
        binding.cardAddNotice.setOnClickListener(v ->
                startActivity(new Intent(this, LecturerNoticesActivity.class)));
        binding.cardAddModule.setOnClickListener(v ->
                startActivity(new Intent(this, CourseListActivity.class)));
    }

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_management) {
                startActivity(new Intent(this, LecturerManagementActivity.class));
                return true;
            } else if (id == R.id.nav_courses) {
                startActivity(new Intent(this, CourseListActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }
}