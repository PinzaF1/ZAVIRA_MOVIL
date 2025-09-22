package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityLevelDetailBinding;
import com.example.zavira_movil.model.Subject;

import java.util.Collections;

public class LevelDetailActivity extends AppCompatActivity {

    private ActivityLevelDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLevelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Recibe la materia y el índice del nivel
        Subject subject = (Subject) getIntent().getSerializableExtra("subject");
        int levelIndex = getIntent().getIntExtra("level_index", -1);

        if (subject == null || subject.levels == null ||
                levelIndex < 0 || levelIndex >= subject.levels.size()) {
            finish();
            return;
        }

        Subject.Level lvl = subject.levels.get(levelIndex);

        // Header con color/gradient de la materia
        if (subject.headerDrawableRes != 0) {
            binding.header.setBackgroundResource(subject.headerDrawableRes);
        }
        binding.tvSubject.setText(subject.title);
        binding.tvLevel.setText(lvl.name);
        binding.tvStatus.setText(lvl.status);

        // (Opcional) descripción del nivel o del área si agregas un TextView con id tvDescription
        if (binding.tvDescription != null) {
            // Usa una descripción del nivel si la tienes; si no, una del área
            String desc = (lvl.status != null && !lvl.status.isEmpty())
                    ? ""  // aquí podrías mapear descripciones por nivel
                    : null;
            if (desc == null || desc.isEmpty()) {
                // si guardaste la descripción del área en subject.description (String)
                // ajusta si tu modelo la tiene como int/string res
                // binding.tvDescription.setText(subject.descriptionStr);
            }
        }

        // Progreso por subtemas
        int percent = lvl.subtopicsPercent();
        binding.progress.setProgress(percent);
        int total = (lvl.subtopics == null) ? 0 : lvl.subtopics.size();
        binding.tvProgress.setText(lvl.subtopicsDone() + "/" + total + " subtemas (" + percent + "%)");

        // Lista de subtemas
        binding.rvSubtopics.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubtopics.setAdapter(
                new SubtopicAdapter(lvl.subtopics != null ? lvl.subtopics : Collections.emptyList())
        );

        // (Opcional) botón "Comenzar" si agregas un MaterialButton con id btnStart en el layout
        if (binding.btnStart != null) {
            binding.btnStart.setOnClickListener(v ->
                    Toast.makeText(this, "Iniciando " + lvl.name + "…", Toast.LENGTH_SHORT).show()
            );
        }
    }
}
