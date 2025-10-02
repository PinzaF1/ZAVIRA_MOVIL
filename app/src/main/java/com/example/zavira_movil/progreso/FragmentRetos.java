package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.Reto;
import com.example.zavira_movil.model.RetoEstado;
import com.example.zavira_movil.model.RetoRonda;
import com.example.zavira_movil.model.RetoRespuesta;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FragmentRetos extends Fragment {

    private Button btnCrearReto, btnAceptarReto, btnResponderRonda, btnVerEstado;
    private TextView tvEstado;

    private ApiService apiService;
    private int ultimoIdReto; // guardamos el último id de reto creado o seleccionado

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_retos, container, false);

        btnCrearReto = view.findViewById(R.id.btnCrearReto);
        btnAceptarReto = view.findViewById(R.id.btnAceptarReto);
        btnResponderRonda = view.findViewById(R.id.btnResponderRonda);
        btnVerEstado = view.findViewById(R.id.btnVerEstado);
        tvEstado = view.findViewById(R.id.tvEstado);

        // Inicializar ApiService usando RetrofitClient
        Retrofit retrofit = RetrofitClient.getInstance(getContext());
        apiService = retrofit.create(ApiService.class);

        btnCrearReto.setOnClickListener(v -> crearReto());
        btnAceptarReto.setOnClickListener(v -> aceptarReto());
        btnResponderRonda.setOnClickListener(v -> responderRonda());
        btnVerEstado.setOnClickListener(v -> verEstadoReto());

        return view;
    }

    //  MÉTODOS

    private void crearReto() {
        Reto nuevoReto = new Reto(2, 1, "basico"); // idUsuarioRetado, idMateria, dificultad
        apiService.crearReto(nuevoReto).enqueue(new Callback<Reto>() {
            @Override
            public void onResponse(Call<Reto> call, Response<Reto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ultimoIdReto = response.body().getId(); // ⚠️ usar getId() según modelo
                    Toast.makeText(getContext(), "Reto creado con ID " + ultimoIdReto, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al crear reto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Reto> call, Throwable t) {
                Toast.makeText(getContext(), "Error crear reto: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void aceptarReto() {
        if (ultimoIdReto == 0) {
            Toast.makeText(getContext(), "Primero crea un reto", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.aceptarReto(ultimoIdReto).enqueue(new Callback<RetoEstado>() {
            @Override
            public void onResponse(Call<RetoEstado> call, Response<RetoEstado> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvEstado.setText("Reto aceptado!\nEstado: " + response.body().getEstado());
                } else {
                    Toast.makeText(getContext(), "Error al aceptar reto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RetoEstado> call, Throwable t) {
                Toast.makeText(getContext(), "Error aceptar reto: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void responderRonda() {
        if (ultimoIdReto == 0) {
            Toast.makeText(getContext(), "Primero crea un reto", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ejemplo: enviamos una ronda con idPregunta=1 y idRespuesta=2
        RetoRonda ronda = new RetoRonda(ultimoIdReto, 1, 2);

        apiService.responderRonda(ronda).enqueue(new Callback<RetoRespuesta>() {
            @Override
            public void onResponse(Call<RetoRespuesta> call, Response<RetoRespuesta> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RetoRespuesta res = response.body();
                    tvEstado.setText("Ronda respondida!\nMi puntaje: " + res.getPuntajeActual()
                            + "\nOponente: " + res.getPuntajeOponente()
                            + "\nAcierto: " + res.isAcierto());
                } else {
                    Toast.makeText(getContext(), "Error al responder ronda", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RetoRespuesta> call, Throwable t) {
                Toast.makeText(getContext(), "Error responder ronda: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verEstadoReto() {
        if (ultimoIdReto == 0) {
            Toast.makeText(getContext(), "Primero crea un reto", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.estadoReto(ultimoIdReto).enqueue(new Callback<RetoEstado>() {
            @Override
            public void onResponse(Call<RetoEstado> call, Response<RetoEstado> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RetoEstado estado = response.body();
                    tvEstado.setText("Estado del Reto:\nEstado: " + estado.getEstado()
                            + "\nJugadores: " + estado.getJugadores()
                            + "\nPuntaje J1: " + estado.getPuntajeJugador1()
                            + "\nPuntaje J2: " + estado.getPuntajeJugador2());
                } else {
                    Toast.makeText(getContext(), "Error al obtener estado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RetoEstado> call, Throwable t) {
                Toast.makeText(getContext(), "Error ver estado: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
