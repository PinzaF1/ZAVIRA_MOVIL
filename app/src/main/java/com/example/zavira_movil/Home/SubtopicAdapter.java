package com.example.zavira_movil.Home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Subject;

import java.util.List;

public class SubtopicAdapter extends RecyclerView.Adapter<SubtopicAdapter.VH> {

    private final List<Subject.Subtopic> data;

    public SubtopicAdapter(List<Subject.Subtopic> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtopic_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Subject.Subtopic s = data.get(position);
        h.tvTitle.setText(s.title);
        h.cbDone.setChecked(s.done);

        // Click futuro: abrir contenido del subtema
        h.itemView.setOnClickListener(v -> {
            // TODO: lanzar actividad de contenido (video/quiz)
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        CheckBox cbDone;
        TextView tvTitle;
        VH(@NonNull View v) {
            super(v);
            cbDone = v.findViewById(R.id.cbDone);
            tvTitle = v.findViewById(R.id.tvSubtopicTitle);
        }
    }
}
