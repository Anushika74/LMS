package com.ati.lms.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.NotificationAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityNotificationsBinding;
import com.ati.lms.models.AppNotification;

import java.util.List;

/**
 * NotificationsActivity - Shows in-app notifications
 */
public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        binding.tvTitle.setText("Notifications");
        binding.btnBack.setOnClickListener(v -> finish());

        loadNotifications();
    }

    private void loadNotifications() {
        List<AppNotification> notifications = dbHelper.getNotifications(50);

        if (notifications.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvNotifications.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvNotifications.setVisibility(View.VISIBLE);
            binding.rvNotifications.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotifications.setAdapter(new NotificationAdapter(notifications, notification -> {
                dbHelper.markNotificationRead(notification.getId());
            }));
        }
    }
}