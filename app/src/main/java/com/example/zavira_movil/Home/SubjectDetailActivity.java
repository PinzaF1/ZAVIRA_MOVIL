package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        binding.rvLevels.setAdapter(new LevelAdapter(s.levels));
    }

    /** Adapter simple para los niveles/subtemas */
    static class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.VH> {
        private final List<Subject.Level> levels;

        LevelAdapter(List<Subject.Level> levels) { this.levels = levels; }

        @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_level_row, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(VH h, int pos) {
            Subject.Level l = levels.get(pos);
            h.tvName.setText(l.name);
            h.tvStatus.setText(l.status);
        }

        @Override public int getItemCount() { return levels == null ? 0 : levels.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvName, tvStatus;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvLevelName);
                tvStatus = v.findViewById(R.id.tvStatus);
            }
        }
    }
}
