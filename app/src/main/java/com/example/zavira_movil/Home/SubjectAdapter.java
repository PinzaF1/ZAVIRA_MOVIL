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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Subject;

import java.util.ArrayList;
import java.util.List;

/**
 * Muestra cada área (tarjeta). Al expandir, lista sus 5 niveles con botón "Comenzar"
 * usando LevelMiniAdapter.
 */
public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.VH> {

    private final List<Subject> data;
    private final SparseBooleanArray expanded = new SparseBooleanArray(); // estado por card

    public SubjectAdapter(List<Subject> data) {
        this.data = (data == null) ? new ArrayList<>() : data;
    }

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

        // ---- Header e info principal
        h.tvTitle.setText(s.title);
        h.ivIcon.setImageResource(s.iconRes);
        h.tvModules.setText(s.done + "/" + s.total + " niveles");
        h.tvPercent.setText(s.percent() + "%");
        h.progress.setProgress(s.percent());
        if (s.headerDrawableRes != 0) {
            h.header.setBackgroundResource(s.headerDrawableRes);
        }

        // ---- Recycler interno con los 5 niveles
        if (h.rvInner.getLayoutManager() == null) {
            h.rvInner.setLayoutManager(new LinearLayoutManager(ctx));
        }
        h.rvInner.setAdapter(new LevelMiniAdapter(s.levels, s)); // << aquí listamos los 5 niveles

        // ---- Expandir/colapsar
        boolean isExpanded = expanded.get(position, false);
        h.rvInner.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        h.ivArrow.setRotation(isExpanded ? 90f : 0f);

        View.OnClickListener toggle = v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            boolean now = !expanded.get(pos, false);
            expanded.put(pos, now);
            h.rvInner.setVisibility(now ? View.VISIBLE : View.GONE);
            h.ivArrow.animate().rotation(now ? 90f : 0f).setDuration(150).start();
        };

        h.ivArrow.setOnClickListener(toggle);
        h.rowHeader.setOnClickListener(toggle);

        // (Opcional) tocar toda la tarjeta abre una pantalla de detalle de niveles
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, SubjectDetailActivity.class);
            i.putExtra("subject", s);
            ctx.startActivity(i);
        });
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View header;                 // FrameLayout flHeader (fondo de color)
        ImageView ivIcon, ivArrow;   // icono y flecha
        TextView tvTitle, tvModules, tvPercent;
        ProgressBar progress;
        RecyclerView rvInner;        // lista con los niveles
        View rowHeader;              // la fila clickable (mismo contenedor del header)

        VH(@NonNull View v) {
            super(v);
            header     = v.findViewById(R.id.flHeader);
            ivIcon     = v.findViewById(R.id.ivIcon);
            ivArrow    = v.findViewById(R.id.ivArrow);
            tvTitle    = v.findViewById(R.id.tvTitle);
            tvModules  = v.findViewById(R.id.tvModules);
            tvPercent  = v.findViewById(R.id.tvPercent);
            progress   = v.findViewById(R.id.progress);
            rvInner    = v.findViewById(R.id.rvSubtopics); // usamos este id para la lista interna
            rowHeader  = v.findViewById(R.id.flHeader);    // tocar el header despliega
        }
    }
}
