package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivitySubjectDetailBinding;
import com.example.zavira_movil.model.Subject;

import java.util.List;

public class SubjectDetailActivity extends AppCompatActivity {

    private ActivitySubjectDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySubjectDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Subject s = (Subject) getIntent().getSerializableExtra("subject");
        if (s == null) { finish(); return; }

        binding.tvSubjectTitle.setText(s.title);
        binding.rvLevels.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLevels.setAdapter(new LevelAdapter(s.levels, s));
    }

    /** Adapter de niveles que abre LevelDetailActivity */
    static class LevelAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<LevelAdapter.VH> {
        private final List<Subject.Level> levels;
        private final Subject subject;

        LevelAdapter(List<Subject.Level> levels, Subject subject) {
            this.levels = levels;
            this.subject = subject;
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_level_row, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Subject.Level l = levels.get(pos);
            h.tvName.setText(l.name);
            h.tvStatus.setText(l.status);

            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), LevelDetailActivity.class);
                i.putExtra("subject", subject);
                i.putExtra("level_index", h.getBindingAdapterPosition());
                v.getContext().startActivity(i);
            });
        }

        @Override public int getItemCount() { return levels.size(); }

        static class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView tvName, tvStatus;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLevelName);
                tvStatus = v.findViewById(R.id.tvLevelStatus);
            }
        }
    }
}
