package com.example.zavira_movil.Home;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.local.ProgressLockManager;
import com.example.zavira_movil.model.Subject;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class LevelMiniAdapter extends RecyclerView.Adapter<LevelMiniAdapter.VH> {

    private final List<Subject.Level> levels;
    private final Subject subject;

    public LevelMiniAdapter(List<Subject.Level> levels, Subject subject) {
        this.levels = levels;
        this.subject = subject;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_level_mini_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Subject.Level l = levels.get(pos);
        int nivelNumero = pos + 1;

        String sub = (l.subtopics != null && !l.subtopics.isEmpty())
                ? l.subtopics.get(0).title
                : "Subtema";

        h.tvLevelName.setText("Nivel " + nivelNumero + ": " + l.name);
        h.tvLevelSubtopic.setText(sub);

        // Detectar si es Simulacro Final
        boolean isSimulacro = "SIMULACRO FINAL".equalsIgnoreCase(sub);

        // Lock normal para niveles 1..5; Simulacro = nivel 6
        boolean enabled = isSimulacro
                ? ProgressLockManager.isLevelUnlocked(h.itemView.getContext(), subject.title, 6)
                : ProgressLockManager.isLevelUnlocked(h.itemView.getContext(), subject.title, nivelNumero);

        h.btnStart.setEnabled(enabled);
        h.btnStart.setAlpha(enabled ? 1f : 0.4f);
        h.btnStart.setText(enabled ? "Comenzar" : "Bloqueado");

        // Click SOLO en el botón
        h.btnStart.setOnClickListener(v -> {
            if (!enabled) return;

            if (isSimulacro) {
                // Abrir actividad del Simulacro Final
                Intent i = new Intent(v.getContext(), SimulacroActivity.class);
                i.putExtra("AREA", subject.title);
                v.getContext().startActivity(i);
            } else {
                // Abrir quiz normal
                Intent i = new Intent(v.getContext(), QuizActivity.class);
                i.putExtra(QuizActivity.EXTRA_AREA, subject.title);
                i.putExtra(QuizActivity.EXTRA_SUBTEMA, sub);
                i.putExtra(QuizActivity.EXTRA_NIVEL, nivelNumero);
                v.getContext().startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (levels == null) ? 0 : levels.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvLevelName, tvLevelSubtopic;
        MaterialButton btnStart;

        VH(@NonNull View v) {
            super(v);
            tvLevelName = v.findViewById(R.id.tvLevelName);
            tvLevelSubtopic = v.findViewById(R.id.tvLevelSubtopic);
            btnStart = v.findViewById(R.id.btnStart);
        }
    }
}
