// app/src/main/java/com/example/zavira_movil/Home/SubjectAdapter.java
package com.example.zavira_movil.Home;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Subject;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.VH> {

    private final List<Subject> data;
    public SubjectAdapter(List<Subject> data) { this.data = data; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Subject s = data.get(position);
        Context ctx = h.itemView.getContext();

        // Header
        h.tvTitle.setText(s.title);
        h.ivIcon.setImageResource(s.iconRes);
        if (s.headerDrawableRes != 0) h.headerClickable.setBackgroundResource(s.headerDrawableRes);

        // Descripción (Subject.description es String)
        h.tvDesc.setText(s.description == null ? "" : s.description);

        // Resumen
        h.tvModules.setText(s.done + "/" + s.total + " niveles");
        h.tvPercent.setText(s.percent() + "%");
        h.progress.setProgress(s.percent());

        // Estado expandido
        h.subtopicsContainer.setVisibility(s.expanded ? View.VISIBLE : View.GONE);
        h.ivChevron.setRotation(s.expanded ? 270f : 90f);

        // ----- Contenido expandido: Niveles + lista de subtemas -----
        h.subtopicsContainer.removeAllViews();
        if (s.expanded && s.levels != null) {
            LayoutInflater inf = LayoutInflater.from(ctx);
            for (int i = 0; i < s.levels.size(); i++) {
                final int levelIdx = i;
                Subject.Level lvl = s.levels.get(i);

                View block = inf.inflate(R.layout.item_level_block, h.subtopicsContainer, false);

                // “Nivel X” pequeño
                TextView tvLevelNumber = block.findViewById(R.id.tvLevelNumber);
                tvLevelNumber.setText("Nivel " + (i + 1));

                // Nombre del subtema (solo el texto después del guion “—” si viene así)
                TextView tvLevelName = block.findViewById(R.id.tvLevelName);
                String levelName = (lvl.name == null) ? "" : lvl.name;
                int dash = levelName.indexOf("—"); // em-dash
                if (dash >= 0) levelName = levelName.substring(dash + 1).trim();
                tvLevelName.setText(levelName);

                // Botón "Comenzar" -> pantalla del nivel (opcional)
                MaterialButton btnStart = block.findViewById(R.id.btnStart);
                btnStart.setOnClickListener(v2 -> {
                    Intent it = new Intent(ctx, LevelDetailActivity.class);
                    it.putExtra("subject", s);
                    it.putExtra("level_index", levelIdx);
                    ctx.startActivity(it);
                });

                // Lista de subtemas debajo
                LinearLayout llSubtopics = block.findViewById(R.id.llSubtopics);
                llSubtopics.removeAllViews();
                if (lvl.subtopics != null) {
                    for (Subject.Subtopic st : lvl.subtopics) {
                        View row = inf.inflate(R.layout.item_subtopic_row, llSubtopics, false);
                        TextView tv = row.findViewById(R.id.tvSubtopicTitle);
                        tv.setText(st.title == null ? "-" : st.title);
                        llSubtopics.addView(row);
                    }
                }

                h.subtopicsContainer.addView(block);
            }
        }

        // Toggle expand/colapse con animación
        View.OnClickListener toggle = v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Subject clicked = data.get(pos);
            clicked.expanded = !clicked.expanded;

            ViewGroup card = (ViewGroup) h.itemView;
            TransitionManager.beginDelayedTransition(card, new AutoTransition().setDuration(180));
            ObjectAnimator.ofFloat(h.ivChevron, View.ROTATION,
                            h.ivChevron.getRotation(), clicked.expanded ? 270f : 90f)
                    .setDuration(180).start();

            notifyItemChanged(pos);
        };
        h.headerClickable.setOnClickListener(toggle);
        h.ivChevron.setOnClickListener(toggle);
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final View headerClickable;
        final ImageView ivIcon, ivChevron;
        final TextView tvTitle, tvDesc, tvModules, tvPercent;
        final ProgressBar progress;
        final LinearLayout subtopicsContainer;

        VH(@NonNull View v) {
            super(v);
            headerClickable    = v.findViewById(R.id.headerClickable);
            ivIcon             = v.findViewById(R.id.ivIcon);
            ivChevron          = v.findViewById(R.id.ivChevron);
            tvTitle            = v.findViewById(R.id.tvTitle);
            tvDesc             = v.findViewById(R.id.tvDescripcion);
            tvModules          = v.findViewById(R.id.tvModules);
            tvPercent          = v.findViewById(R.id.tvPercent);
            progress           = v.findViewById(R.id.progress);
            subtopicsContainer = v.findViewById(R.id.subtopicsContainer);
        }
    }
}
