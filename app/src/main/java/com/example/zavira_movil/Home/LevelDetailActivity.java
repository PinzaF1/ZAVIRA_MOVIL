package com.example.zavira_movil.Home;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityLevelDetailBinding;
import com.example.zavira_movil.model.Subject;

public class LevelDetailActivity extends AppCompatActivity {

    private ActivityLevelDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLevelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Subject subject = (Subject) getIntent().getSerializableExtra("subject");
        int levelIndex = getIntent().getIntExtra("level_index", -1);

        if (subject == null || levelIndex < 0 || levelIndex >= subject.levels.size()) {
            finish();
            return;
        }

        Subject.Level lvl = subject.levels.get(levelIndex);

        // Header a color por materia
        binding.header.setBackgroundResource(subject.headerDrawableRes);
        binding.tvSubject.setText(subject.title);
        binding.tvLevel.setText(lvl.name);
        binding.tvStatus.setText(lvl.status);

        // Progreso por subtemas
        binding.progress.setProgress(lvl.subtopicsPercent());
        int total = (lvl.subtopics == null) ? 0 : lvl.subtopics.size();
        binding.tvProgress.setText(lvl.subtopicsDone() + "/" + total + " subtemas (" + lvl.subtopicsPercent() + "%)");

        // Lista
        binding.rvSubtopics.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubtopics.setAdapter(new SubtopicAdapter(lvl.subtopics));
    }
}
