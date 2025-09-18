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

/** Adapter para mostrar los subtemas dentro de un nivel/área */
public class SubtopicAdapter extends RecyclerView.Adapter<SubtopicAdapter.VH> {

    private final List<Subject.Subtopic> data;

    public SubtopicAdapter(@NonNull List<Subject.Subtopic> data) {
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
        h.tvTitle.setText(s.title != null ? s.title : "-");
        h.cbDone.setChecked(s.done);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final CheckBox cbDone;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSubtopicTitle);   // ojo: id en item_subtopic_row.xml
            cbDone  = itemView.findViewById(R.id.cbDone);
        }
    }
}
