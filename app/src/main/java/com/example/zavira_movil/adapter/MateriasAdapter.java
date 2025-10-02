package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.MateriaDetalle;

import java.util.List;

public class MateriasAdapter extends RecyclerView.Adapter<MateriasAdapter.MateriaViewHolder> {

    private List<MateriaDetalle> lista;

    public MateriasAdapter(List<MateriaDetalle> lista) {
        this.lista = lista;
    }

    public void setLista(List<MateriaDetalle> nuevaLista) {
        this.lista = nuevaLista;
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_materia, parent, false);
        return new MateriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        MateriaDetalle materia = lista.get(position);
        holder.tvNombre.setText(materia.getNombre());
        holder.tvEtiqueta.setText(materia.getEtiqueta());
        holder.tvProgreso.setText(materia.getPorcentaje() + "%");
        holder.progress.setProgress(materia.getPorcentaje());
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class MateriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEtiqueta, tvProgreso;
        ProgressBar progress;

        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvEtiqueta = itemView.findViewById(R.id.tvEtiqueta);
            tvProgreso = itemView.findViewById(R.id.tvProgreso);
            progress = itemView.findViewById(R.id.progressMateria);
        }
    }
}
