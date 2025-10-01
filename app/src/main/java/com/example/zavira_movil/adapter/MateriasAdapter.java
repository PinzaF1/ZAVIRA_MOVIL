package com.example.zavira_movil.adapter;

import android.content.Context;
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

public class MateriasAdapter extends RecyclerView.Adapter<MateriasAdapter.MateriaViewHolder> {

    private Context context;
    private List<MateriaProgreso> materias;

    public MateriasAdapter(Context context, List<MateriaProgreso> materias) {
        this.context = context;
        this.materias = materias;
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_materia, parent, false);
        return new MateriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        MateriaProgreso materia = materias.get(position);
        int porcentaje = materia.getPorcentaje();

        // Nombre y porcentaje
        holder.txtNombreMateria.setText(materia.getNombre());
        holder.txtProgreso.setText(porcentaje + "%");
        holder.progressMateria.setProgress(porcentaje);

        // Descripción dinámica según porcentaje
        String descripcion;
        if (porcentaje >= 90) {
            descripcion = "Excelente";
            holder.progressMateria.setProgressTintList(context.getResources().getColorStateList(R.color.morado));
        } else if (porcentaje >= 70) {
            descripcion = "Buen progreso";
            holder.progressMateria.setProgressTintList(context.getResources().getColorStateList(R.color.azul));
        } else if (porcentaje >= 50) {
            descripcion = "Progreso aceptable";
            holder.progressMateria.setProgressTintList(context.getResources().getColorStateList(R.color.verde));
        } else if (porcentaje >= 30) {
            descripcion = "Necesita mejorar";
            holder.progressMateria.setProgressTintList(context.getResources().getColorStateList(R.color.naranja));
        } else {
            descripcion = "Requiere atención";
            holder.progressMateria.setProgressTintList(context.getResources().getColorStateList(R.color.rojo));
        }
        holder.txtDescripcion.setText(descripcion);
    }

    @Override
    public int getItemCount() {
        return materias == null ? 0 : materias.size();
    }

    static class MateriaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreMateria, txtProgreso, txtDescripcion;
        ProgressBar progressMateria;

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombreMateria = itemView.findViewById(R.id.txtNombreMateria);
            txtProgreso = itemView.findViewById(R.id.txtProgreso);
            progressMateria = itemView.findViewById(R.id.progressMateria);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcion);
        }
    }
}
