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

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialItem item = lista.get(position);
        holder.tvActividad.setText(item.getActividad());
        holder.tvFecha.setText(item.getFecha());
        holder.tvEstado.setText(item.isCompletada() ? "Completada" : "Pendiente");
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvActividad, tvFecha, tvEstado;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActividad = itemView.findViewById(R.id.tvActividadHistorial);
            tvFecha = itemView.findViewById(R.id.tvFechaHistorial);
            tvEstado = itemView.findViewById(R.id.tvEstadoHistorial);
        }
    }
}
