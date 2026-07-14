package com.ati.lms.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ati.lms.R;
import com.ati.lms.adapters.PastPapersAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityPastPapersBinding;
import com.ati.lms.models.PastPaper;
import com.ati.lms.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * PastPapersActivity - Student view for browsing and downloading past papers
 */
public class PastPapersActivity extends AppCompatActivity {

    private ActivityPastPapersBinding binding;
    private DatabaseHelper dbHelper;
    private int moduleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPastPapersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        moduleId = getIntent().getIntExtra("module_id", 0);
        String moduleName = getIntent().getStringExtra("module_name");

        binding.tvTitle.setText(moduleName + " - Past Papers");
        binding.btnBack.setOnClickListener(v -> finish());

        setupSearch();
        loadPastPapers();
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchPapers(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) loadPastPapers();
                else searchPapers(newText);
                return true;
            }
        });
    }

    private void loadPastPapers() {
        List<PastPaper> papers = dbHelper.getPastPapersByModule(moduleId);
        displayPapers(papers);
    }

    private void searchPapers(String query) {
        List<PastPaper> papers = dbHelper.searchPastPapers(query);
        displayPapers(papers);
    }

    private void displayPapers(List<PastPaper> papers) {
        if (papers.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvPastPapers.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvPastPapers.setVisibility(View.VISIBLE);
            binding.rvPastPapers.setLayoutManager(new LinearLayoutManager(this));
            binding.rvPastPapers.setAdapter(new PastPapersAdapter(papers, new PastPapersAdapter.OnPastPaperClickListener() {
                @Override
                public void onViewClick(PastPaper paper) {
                    viewFile(paper.getFilePath());
                }

                @Override
                public void onDownloadClick(PastPaper paper) {
                    downloadFile(paper.getFilePath());
                }
            }));
        }
    }

    private void viewFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, FileUtils.getMimeType(filePath));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app available to open this file", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadFile(String filePath) {
        String exported = FileUtils.exportToDownloads(this, filePath);
        if (exported != null) {
            Toast.makeText(this, "Downloaded to: " + exported, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }
}