package com.example.zavira_movil.adapter;

import android.content.Context;
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

    private Context context;
    private List<HistorialItem> historialList;

    public HistorialAdapter(Context context, List<HistorialItem> historialList) {
        this.context = context;
        this.historialList = historialList;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialItem item = historialList.get(position);

        holder.txtMateria.setText(item.getMateria());
        holder.txtFecha.setText(item.getFecha());
        holder.txtPorcentaje.setText(item.getPorcentaje() + "%");

        // Nivel y color según porcentaje
        String nivel;
        int colorCircle;

        if (item.getPorcentaje() >= 90) {
            nivel = "Experto";
            colorCircle = context.getResources().getColor(R.color.blue);
        } else if (item.getPorcentaje() >= 70) {
            nivel = "Avanzado";
            colorCircle = context.getResources().getColor(R.color.green);
        } else if (item.getPorcentaje() >= 50) {
            nivel = "Intermedio";
            colorCircle = context.getResources().getColor(R.color.orange);
        } else {
            nivel = "Básico";
            colorCircle = context.getResources().getColor(R.color.red);
        }

        holder.txtNivel.setText(nivel);
        holder.circuloNivel.setBackgroundColor(colorCircle);
    }

    @Override
    public int getItemCount() {
        return historialList.size();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView txtMateria, txtFecha, txtPorcentaje, txtNivel;
        View circuloNivel;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMateria = itemView.findViewById(R.id.txtMateria);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtPorcentaje = itemView.findViewById(R.id.txtPorcentaje);
            txtNivel = itemView.findViewById(R.id.txtNivel);
            circuloNivel = itemView.findViewById(R.id.circuloNivel);
        }
    }
}
