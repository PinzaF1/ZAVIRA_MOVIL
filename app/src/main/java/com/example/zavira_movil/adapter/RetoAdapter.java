package com.example.zavira_movil.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.RetoConRondas;
import com.example.zavira_movil.model.RetoRonda;
import com.example.zavira_movil.model.RetoRespuesta;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RetoAdapter extends RecyclerView.Adapter<RetoAdapter.RetoViewHolder> {

    private List<RetoConRondas> retoList;
    private ApiService apiService;

    public RetoAdapter(List<RetoConRondas> retoList, Context context) {
        this.retoList = retoList;

        // Inicializar ApiService usando RetrofitClient
        Retrofit retrofit = RetrofitClient.getInstance(context);
        this.apiService = retrofit.create(ApiService.class);
    }

    @NonNull
    @Override
    public RetoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reto, parent, false);
        return new RetoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RetoViewHolder holder, int position) {
        RetoConRondas reto = retoList.get(position);

        holder.tvRetoId.setText("Reto #" + reto.getIdReto());
        holder.tvEstado.setText("Estado: " + reto.getEstado());
        holder.tvPuntaje.setText("P1: " + reto.getPuntajeJugador1() + " | P2: " + reto.getPuntajeJugador2());

        holder.btnResponderRonda.setOnClickListener(v -> {
            if (!reto.getRondas().isEmpty()) {
                RetoRonda ronda = reto.getRondas().get(0); // Tomamos la primera ronda como ejemplo

                apiService.responderRonda(ronda).enqueue(new Callback<RetoRespuesta>() {
                    @Override
                    public void onResponse(Call<RetoRespuesta> call, Response<RetoRespuesta> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RetoRespuesta res = response.body();

                            holder.tvPuntaje.setText("Mi puntaje: " + res.getPuntajeActual()
                                    + " | Oponente: " + res.getPuntajeOponente());

                            Toast.makeText(holder.itemView.getContext(),
                                    res.isAcierto() ? "¡Acertaste!" : "Fallaste",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(holder.itemView.getContext(),
                                    "Error al responder la ronda", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RetoRespuesta> call, Throwable t) {
                        Toast.makeText(holder.itemView.getContext(),
                                "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(holder.itemView.getContext(),
                        "No hay rondas disponibles", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return retoList.size();
    }

    static class RetoViewHolder extends RecyclerView.ViewHolder {
        TextView tvRetoId, tvEstado, tvPuntaje;
        Button btnResponderRonda;

        public RetoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRetoId = itemView.findViewById(R.id.tvRetoId);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvPuntaje = itemView.findViewById(R.id.tvPuntaje);
            btnResponderRonda = itemView.findViewById(R.id.btnResponderRonda);
        }
    }
}
