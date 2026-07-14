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
import com.ati.lms.adapters.NotesAdapter;
import com.ati.lms.database.DatabaseHelper;
import com.ati.lms.databinding.ActivityNotesBinding;
import com.ati.lms.models.Note;
import com.ati.lms.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * NotesActivity - Student view for browsing and downloading notes
 */
public class NotesActivity extends AppCompatActivity {

    private ActivityNotesBinding binding;
    private DatabaseHelper dbHelper;
    private int moduleId;
    private String moduleName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = DatabaseHelper.getInstance(this);
        moduleId = getIntent().getIntExtra("module_id", 0);
        moduleName = getIntent().getStringExtra("module_name");

        binding.tvTitle.setText(moduleName + " - Notes");
        binding.btnBack.setOnClickListener(v -> finish());

        setupSearch();
        loadNotes();
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchNotes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) loadNotes();
                else searchNotes(newText);
                return true;
            }
        });
    }

    private void loadNotes() {
        List<Note> notes = dbHelper.getNotesByModule(moduleId);
        displayNotes(notes);
    }

    private void searchNotes(String query) {
        List<Note> notes = dbHelper.searchNotes(query);
        displayNotes(notes);
    }

    private void displayNotes(List<Note> notes) {
        if (notes.isEmpty()) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.rvNotes.setVisibility(View.GONE);
        } else {
            binding.tvEmpty.setVisibility(View.GONE);
            binding.rvNotes.setVisibility(View.VISIBLE);
            binding.rvNotes.setLayoutManager(new LinearLayoutManager(this));
            binding.rvNotes.setAdapter(new NotesAdapter(notes, new NotesAdapter.OnNoteClickListener() {
                @Override
                public void onViewClick(Note note) {
                    viewFile(note.getFilePath());
                }

                @Override
                public void onDownloadClick(Note note) {
                    downloadFile(note.getFilePath(), note.getTitle());
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

    private void downloadFile(String filePath, String title) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }

        String exported = FileUtils.exportToDownloads(this, filePath);
        if (exported != null) {
            Toast.makeText(this, "Downloaded to: " + exported, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        }
    }
}