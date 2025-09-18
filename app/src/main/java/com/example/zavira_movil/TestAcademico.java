package com.example.zavira_movil;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestAcademico extends AppCompatActivity {

    private RecyclerView rvPreguntas;
    private Button btnEnviar;
    private PreguntasAdapter adapter;
    private final List<PreguntaAcademica> preguntas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_academico);

        rvPreguntas = findViewById(R.id.rvPreguntas);
        btnEnviar = findViewById(R.id.btnEnviar);

        rvPreguntas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PreguntasAdapter(this, preguntas);
        rvPreguntas.setAdapter(adapter);

        btnEnviar.setOnClickListener(v -> enviar());

        cargar();
    }

    private void cargar() {
        btnEnviar.setEnabled(false);
        btnEnviar.setText("Cargando...");

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.getPreguntaAcademica().enqueue(new Callback<List<PreguntaAcademica>>() {
            @Override
            public void onResponse(Call<List<PreguntaAcademica>> call, Response<List<PreguntaAcademica>> response) {
                btnEnviar.setText("Enviar respuestas");
                if (!response.isSuccessful() || response.body() == null) {
                    toast("No se pudieron cargar las preguntas");
                    return;
                }

                // Lista cruda del backend
                List<PreguntaAcademica> todas = response.body();

                // Filtro: máximo 5 preguntas por cada área
                Map<String, Integer> contador = new HashMap<>();
                List<PreguntaAcademica> filtradas = new ArrayList<>();

                for (PreguntaAcademica p : todas) {
                    int count = contador.getOrDefault(p.getArea(), 0);
                    if (count < 5) {
                        filtradas.add(p);
                        contador.put(p.getArea(), count + 1);
                    }
                }

                preguntas.clear();
                preguntas.addAll(filtradas);

                adapter.notifyDataSetChanged();
                btnEnviar.setEnabled(true);

                toast("Se cargaron " + preguntas.size() + " preguntas en total ✅");
            }

            @Override
            public void onFailure(Call<List<PreguntaAcademica>> call, Throwable t) {
                btnEnviar.setText("Enviar respuestas");
                toast("Error: " + t.getMessage());
            }
        });
    }

    private void enviar() {
        int[] respuestas = adapter.getRespuestas();

        for (int r : respuestas) {
            if (r == 0) {
                toast("Responde todas las preguntas antes de enviar");
                return;
            }
        }

        // TODO: aquí vas a enviar las respuestas al backend cuando tengas la ruta lista
        toast("Respuestas enviadas correctamente ✅");
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
