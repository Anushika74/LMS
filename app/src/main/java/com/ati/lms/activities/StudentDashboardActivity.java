package com.ati.lms.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.NoticeAdapter;
import com.ati.lms.adapters.QuizResultAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityStudentDashboardBinding;
import com.ati.lms.models.Notice;
import com.ati.lms.models.QuizResult;
import com.ati.lms.models.User;
import com.ati.lms.utils.SessionManager;

import java.io.File;
import java.util.List;

/**
 * StudentDashboardActivity - Main home screen for students
 * Shows greeting, progress, notices, assignments, quiz results
 */
public class StudentDashboardActivity extends AppCompatActivity {

    private ActivityStudentDashboardBinding binding;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        setupUI();
        setupBottomNavigation();
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
        loadDashboardData();
    }

    private void setupUI() {
        // Notification bell
        binding.ivNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
    }

    private void loadProfile() {
        User user = dbHelper.getUserById(sessionManager.getUserId());
        if (user != null) {
            binding.tvGreeting.setText(getGreeting());
            binding.tvStudentName.setText(user.getFullName());

            // Load profile image
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

    private void setupBottomNavigation() {
        binding.bottomNav.setSelectedItemId(R.id.nav_home);
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_courses) {
                startActivity(new Intent(this, CourseListActivity.class));
                return true;
            } else if (id == R.id.nav_assignments) {
                startActivity(new Intent(this, AssignmentsActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadDashboardData() {
        int userId = sessionManager.getUserId();

        // Course progress (based on quiz average)
        double avgScore = dbHelper.getStudentAverageScore(userId);
        binding.progressBar.setProgress((int) avgScore);
        binding.tvProgress.setText(String.format("%.0f%%", avgScore));

        // Notification badge
        int unreadCount = dbHelper.getUnreadNotificationCount();
        if (unreadCount > 0) {
            binding.tvNotificationBadge.setVisibility(View.VISIBLE);
            binding.tvNotificationBadge.setText(String.valueOf(unreadCount));
        } else {
            binding.tvNotificationBadge.setVisibility(View.GONE);
        }

        // Recent notices
        List<Notice> notices = dbHelper.getRecentNotices(3);
        if (!notices.isEmpty()) {
            binding.rvNotices.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotices.setAdapter(new NoticeAdapter(notices, null, false));
            binding.tvNoNotices.setVisibility(View.GONE);
        } else {
            binding.tvNoNotices.setVisibility(View.VISIBLE);
        }

        // Recent quiz results
        List<QuizResult> results = dbHelper.getQuizResultsByStudent(userId);
        if (!results.isEmpty()) {
            List<QuizResult> recentResults = results.subList(0, Math.min(3, results.size()));
            binding.rvQuizResults.setLayoutManager(new LinearLayoutManager(this));
            binding.rvQuizResults.setAdapter(new QuizResultAdapter(recentResults));
            binding.tvNoQuizResults.setVisibility(View.GONE);
        } else {
            binding.tvNoQuizResults.setVisibility(View.VISIBLE);
        }
    }

    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }
}

