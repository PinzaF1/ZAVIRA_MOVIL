package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.HistorialItem;

import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<HistorialItem> lista;

    public HistorialAdapter(List<HistorialItem> lista) {
        this.lista = lista;
    }

    public void setLista(List<HistorialItem> nuevaLista) {
        this.lista = nuevaLista;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialItem item = lista.get(position);
        holder.tvMateria.setText(item.getMateria());
        holder.tvNivel.setText(item.getNivel());
        holder.tvPorcentaje.setText(item.getPorcentaje() + "%");
        holder.tvFecha.setText(item.getFecha());
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvMateria, tvNivel, tvPorcentaje, tvFecha;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMateria = itemView.findViewById(R.id.tvMateria);
            tvNivel = itemView.findViewById(R.id.tvNivel);
            tvPorcentaje = itemView.findViewById(R.id.tvPorcentaje);
            tvFecha = itemView.findViewById(R.id.tvFecha);
        }
    }
}
