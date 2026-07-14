package com.ati.lms.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.NoticeAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityNoticesBinding;
import com.ati.lms.models.Notice;

import java.util.List;

/**
 * NoticesActivity - Displays all notices for students
 */
public class NoticesActivity extends AppCompatActivity {

    private ActivityNoticesBinding binding;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoticesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        binding.tvTitle.setText("Notices");
        binding.btnBack.setOnClickListener(v -> finish());

        loadNotices();
    }

    private void loadNotices() {
        List<Notice> notices = dbHelper.getAllNotices();

        if (notices.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvNotices.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvNotices.setVisibility(View.VISIBLE);
            binding.rvNotices.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotices.setAdapter(new NoticeAdapter(notices, null, false));
        }
    }
}