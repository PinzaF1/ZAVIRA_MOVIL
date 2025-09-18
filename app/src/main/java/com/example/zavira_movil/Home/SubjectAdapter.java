package com.example.zavira_movil.Home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        h.tvTitle.setText(s.title);
        h.ivIcon.setImageResource(s.iconRes);
        h.tvModules.setText(s.done + "/" + s.total + " niveles");
        h.tvPercent.setText(s.percent() + "%");
        h.progress.setProgress(s.percent());

        if (s.headerDrawableRes != 0) {
            h.header.setBackgroundResource(s.headerDrawableRes); // color por materia
        }

        View.OnClickListener go = v -> {
            Intent i = new Intent(ctx, SubjectDetailActivity.class);
            i.putExtra("subject", s);
            ctx.startActivity(i);
        };
        h.btnExplore.setOnClickListener(go);
        h.itemView.setOnClickListener(go);
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View header;
        ImageView ivIcon;
        TextView tvTitle, tvModules, tvPercent;
        ProgressBar progress;
        MaterialButton btnExplore;
        VH(@NonNull View v) {
            super(v);
            header = v.findViewById(R.id.flHeader);
            ivIcon = v.findViewById(R.id.ivIcon);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvModules = v.findViewById(R.id.tvModules);
            tvPercent = v.findViewById(R.id.tvPercent);
            progress = v.findViewById(R.id.progress);
            btnExplore = v.findViewById(R.id.btnExplore);
        }
    }
}
