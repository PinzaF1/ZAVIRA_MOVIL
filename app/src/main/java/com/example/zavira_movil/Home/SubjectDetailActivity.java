package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivitySubjectDetailBinding;
import com.example.zavira_movil.local.ProgressLockManager;
import com.example.zavira_movil.model.Subject;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SubjectDetailActivity extends AppCompatActivity {

    private ActivitySubjectDetailBinding binding;
    private LevelAdapter adapter;
    private Subject subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        subject = (Subject) getIntent().getSerializableExtra("subject");
        if (subject == null) { finish(); return; }

        binding.tvSubjectTitle.setText(subject.title);
        binding.rvLevels.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LevelAdapter(subject.levels, subject);
        binding.rvLevels.setAdapter(adapter);
    }

    @Override protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged(); // refresca bloqueo tras volver del quiz
    }

    /** Lista “Nivel 1 … Nivel 5” + Examen Final */
    static class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.VH> {
        private final List<Subject.Level> levels;
        private final Subject subject;

        LevelAdapter(List<Subject.Level> levels, Subject subject) {
            this.levels = levels;
            this.subject = subject;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_level_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            if (pos < levels.size()) {
                // ---------------- Niveles normales ----------------
                Subject.Level l = levels.get(pos);
                int nivelNumero = pos + 1;

                h.tvName.setText("Nivel " + nivelNumero);
                String sub = (l.subtopics != null && !l.subtopics.isEmpty())
                        ? l.subtopics.get(0).title
                        : "—";
                h.tvSubtopic.setText(sub);

                boolean unlocked = ProgressLockManager.isLevelUnlocked(
                        h.itemView.getContext(), subject.title, nivelNumero);

                h.btnStart.setEnabled(unlocked);
                h.btnStart.setAlpha(unlocked ? 1f : 0.5f);
                h.btnStart.setText(unlocked ? "Comenzar" : "Bloqueado");

                h.btnStart.setOnClickListener(v -> {
                    if (!unlocked) return;
                    Intent i = new Intent(v.getContext(), QuizActivity.class);
                    i.putExtra(QuizActivity.EXTRA_AREA, subject.title);
                    i.putExtra(QuizActivity.EXTRA_SUBTEMA, sub);
                    i.putExtra(QuizActivity.EXTRA_NIVEL, nivelNumero);
                    v.getContext().startActivity(i);
                });

            } else {
                // ---------------- Examen Final ----------------
                h.tvName.setText("Examen Final");
                h.tvSubtopic.setText("Simulacro de " + subject.title);

                // 🔓 Siempre desbloqueado (modo prueba)
                // Para modo real: usa esta línea en vez de la de abajo:
                // boolean unlocked = ProgressLockManager.getUnlockedLevel(h.itemView.getContext(), subject.title) >= 5;
                boolean unlocked = true;

                h.btnStart.setEnabled(unlocked);
                h.btnStart.setAlpha(unlocked ? 1f : 0.5f);
                h.btnStart.setText(unlocked ? "Comenzar" : "Bloqueado");

                h.btnStart.setOnClickListener(v -> {
                    if (!unlocked) return;
                    Intent i = new Intent(v.getContext(), SimulacroActivity.class);
                    i.putExtra("area", subject.title);
                    v.getContext().startActivity(i);
                });
            }
        }

        @Override public int getItemCount() {
            // niveles + examen final
            return (levels == null ? 0 : levels.size()) + 1;
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvSubtopic;
            MaterialButton btnStart;
            VH(@NonNull View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLevelName);
                tvSubtopic = v.findViewById(R.id.tvLevelSubtopic);
                btnStart = v.findViewById(R.id.btnStart);
            }
        }
    }
}
