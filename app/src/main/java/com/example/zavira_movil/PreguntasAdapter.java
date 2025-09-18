package com.example.zavira_movil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PreguntasAdapter extends RecyclerView.Adapter<PreguntasAdapter.ViewHolder> {

    private final Context context;
    private final List<PreguntaAcademica> preguntas;
    private final int[] respuestas; // almacena la respuesta seleccionada

    public PreguntasAdapter(Context context, List<PreguntaAcademica> preguntas) {
        this.context = context;
        this.preguntas = preguntas;
        this.respuestas = new int[preguntas.size()];
    }

    public int[] getRespuestas() {
        return respuestas;
    }

    @NonNull
    @Override
    public PreguntasAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_pregunta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PreguntasAdapter.ViewHolder holder, int position) {
        PreguntaAcademica p = preguntas.get(position);
        holder.tvPregunta.setText((position + 1) + ". " + p.getPregunta());


        holder.rgOpciones.removeAllViews();

        // usar método getOpciones()
        List<String> opciones = p.getOpciones();
        for (int i = 0; i < opciones.size(); i++) {
            RadioButton rb = new RadioButton(context);
            rb.setText(opciones.get(i));
            int index = i + 1; // respuesta seleccionada
            rb.setOnClickListener(v -> respuestas[position] = index);
            holder.rgOpciones.addView(rb);
        }
    }

    @Override
    public int getItemCount() {
        return preguntas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPregunta;
        RadioGroup rgOpciones;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPregunta = itemView.findViewById(R.id.tvPregunta);
            rgOpciones = itemView.findViewById(R.id.rgOpciones);
        }
    }
}
