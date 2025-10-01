package com.example.zavira_movil.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.MateriaProgreso;

import java.util.List;

public class ProgresoAdapter extends RecyclerView.Adapter<ProgresoAdapter.ViewHolder> {

    private final List<MateriaProgreso> listaMaterias;

    public ProgresoAdapter(List<MateriaProgreso> listaMaterias) {
        this.listaMaterias = listaMaterias;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MateriaProgreso m = listaMaterias.get(position);

        holder.tvNombre.setText(m.getNombre());  // Nombre de la materia
        holder.tvPorcentaje.setText(m.getPorcentaje() + "%"); // Porcentaje
        holder.progressBar.setProgress(m.getPorcentaje()); // Barra de progreso

        // Color según porcentaje
        int p = m.getPorcentaje();
        int color;
        if (p >= 75) color = Color.parseColor("#1976D2"); // azul
        else if (p >= 60) color = Color.parseColor("#388E3C"); // verde
        else color = Color.parseColor("#D32F2F"); // rojo

        holder.progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        holder.tvPorcentaje.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return listaMaterias == null ? 0 : listaMaterias.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvPorcentaje, tvDescripcion;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.txtNombreMateria);
            tvPorcentaje = itemView.findViewById(R.id.txtProgreso);
            tvDescripcion = itemView.findViewById(R.id.txtDescripcion);
            progressBar = itemView.findViewById(R.id.progressMateria);
        }
    }
}
