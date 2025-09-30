package com.example.zavira_movil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.ProgresoMateria;

import java.util.List;

public class ProgresoAdapter extends RecyclerView.Adapter<ProgresoAdapter.ProgresoViewHolder> {

    private List<ProgresoMateria> lista;

    public ProgresoAdapter(List<ProgresoMateria> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ProgresoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_materia, parent, false);
        return new ProgresoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProgresoViewHolder holder, int position) {
        ProgresoMateria item = lista.get(position);
        holder.tvNombre.setText(item.getNombre());
        holder.tvProgreso.setText(item.getProgreso() + "%");
        // Para imagen, si tienes url:
        // Picasso.get().load(item.getImagenUrl()).placeholder(R.drawable.ic_materia_default).into(holder.imgMateria);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ProgresoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvProgreso;
        ImageView imgMateria;

        public ProgresoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreMateria);
            tvProgreso = itemView.findViewById(R.id.tvProgresoMateria);
            imgMateria = itemView.findViewById(R.id.imgMateria);
        }
    }
}
