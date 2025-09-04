package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.KolbRequest;
import com.example.zavira_movil.model.KolbResponse;
import com.example.zavira_movil.model.PreguntasKolb;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {

    private LinearLayout containerPreguntas;
    private Button btnEnviar;
    private TextView tvProgreso;

    private List<PreguntasKolb> listaPreguntas = new ArrayList<>();
    private final Map<Integer, Integer> respuestasPorIndice = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        containerPreguntas = findViewById(R.id.questionsContainer);
        btnEnviar = findViewById(R.id.btnEnviar);

        cargarPreguntas();

        btnEnviar.setOnClickListener(v -> enviarRespuestas());
    }

    private void cargarPreguntas() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        api.getPreguntas().enqueue(new Callback<List<PreguntasKolb>>() {
            @Override
            public void onResponse(Call<List<PreguntasKolb>> call, Response<List<PreguntasKolb>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPreguntas = response.body();

                    Collections.sort(listaPreguntas, (p1, p2) ->
                            Integer.compare(p1.getId_pregunta_estilo_aprendizajes(),
                                    p2.getId_pregunta_estilo_aprendizajes())
                    );

                    // Verifica IDs > 0
                    for (int i = 0; i < Math.min(10, listaPreguntas.size()); i++) {
                        Log.d("KOLB_IDS_DEBUG", "idx=" + i + " id=" +
                                listaPreguntas.get(i).getId_pregunta_estilo_aprendizajes());
                    }

                    mostrarPreguntas();
                } else {
                    Toast.makeText(TestActivity.this, "No se pudieron cargar las preguntas.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<PreguntasKolb>> call, Throwable t) {
                Log.e("Kolb", "Error cargando preguntas", t);
                Toast.makeText(TestActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarPreguntas() {
        containerPreguntas.removeAllViews();

        tvProgreso = new TextView(this);
        tvProgreso.setText("0 / " + listaPreguntas.size());
        tvProgreso.setTextSize(16f);
        tvProgreso.setPadding(0, 0, 0, 32);
        containerPreguntas.addView(tvProgreso);

        for (int idx = 0; idx < listaPreguntas.size(); idx++) {
            final int index = idx;
            PreguntasKolb p = listaPreguntas.get(idx);

            TextView tvTitulo = new TextView(this);
            tvTitulo.setText(p.getTitulo() != null ? p.getTitulo() : p.getTipo_pregunta());
            tvTitulo.setTextSize(15f);
            tvTitulo.setPadding(0, 16, 0, 4);

            TextView tvPregunta = new TextView(this);
            tvPregunta.setText(p.getPregunta());
            tvPregunta.setTextSize(14f);
            tvPregunta.setPadding(0, 4, 0, 8);

            RadioGroup rgOpciones = new RadioGroup(this);
            rgOpciones.setId(View.generateViewId());
            rgOpciones.setOrientation(RadioGroup.HORIZONTAL);

            for (int i = 1; i <= 4; i++) {
                RadioButton rb = new RadioButton(this);
                rb.setId(View.generateViewId());
                rb.setText(String.valueOf(i));
                rb.setTag(i);
                rgOpciones.addView(rb);
            }

            if (respuestasPorIndice.containsKey(index)) {
                int valorPrevio = respuestasPorIndice.get(index);
                int childIdx = valorPrevio - 1;
                if (childIdx >= 0 && childIdx < rgOpciones.getChildCount()) {
                    ((RadioButton) rgOpciones.getChildAt(childIdx)).setChecked(true);
                }
            }

            rgOpciones.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton seleccionado = group.findViewById(checkedId);
                if (seleccionado != null) {
                    int valor = (int) seleccionado.getTag();
                    respuestasPorIndice.put(index, valor);
                    actualizarProgreso();

                    int idBackend = listaPreguntas.get(index).getId_pregunta_estilo_aprendizajes();
                    Log.d("KOLB_PROGRESO",
                            "index=" + index + " idBackend=" + idBackend + " → valor=" + valor
                                    + " | respondidas=" + respuestasPorIndice.size());
                }
            });

            containerPreguntas.addView(tvTitulo);
            containerPreguntas.addView(tvPregunta);
            containerPreguntas.addView(rgOpciones);
        }
    }

    private void actualizarProgreso() {
        int respondidas = respuestasPorIndice.size();
        int total = listaPreguntas.size();
        tvProgreso.setText(respondidas + " / " + total);
        tvProgreso.setTextColor(getResources().getColor(
                respondidas == total ? android.R.color.holo_green_dark : android.R.color.holo_red_dark
        ));
    }

    private void enviarRespuestas() {
        if (respuestasPorIndice.size() != listaPreguntas.size()) {
            Toast.makeText(this, "Debes responder TODAS las preguntas antes de enviar.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar userId
        int userId = TokenManager.getUserId(this);
        if (userId <= 0) {
            String token = TokenManager.getToken(this);
            int fromJwt = TokenManager.extractUserIdFromJwt(token);
            if (fromJwt > 0) {
                userId = fromJwt;
                TokenManager.setUserId(this, userId);
            }
        }
        if (userId <= 0) {
            Toast.makeText(this, "Usuario inválido. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validar que no haya IDs 0
        for (int i = 0; i < listaPreguntas.size(); i++) {
            int idBackend = listaPreguntas.get(i).getId_pregunta_estilo_aprendizajes();
            if (idBackend <= 0) {
                Log.e("KOLB_ID_INVALIDO", "idx=" + i + " idBackend=" + idBackend);
                Toast.makeText(this, "Error: hay preguntas con ID=0. Revisa el mapeo del modelo.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        // Construir payload
        List<KolbRequest.Respuesta> listaRespuestas = new ArrayList<>();
        for (int i = 0; i < listaPreguntas.size(); i++) {
            int idBackend = listaPreguntas.get(i).getId_pregunta_estilo_aprendizajes();
            Integer valor = respuestasPorIndice.get(i);
            listaRespuestas.add(new KolbRequest.Respuesta(idBackend, valor));
        }

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        KolbRequest request = new KolbRequest(userId, listaRespuestas);

        api.guardarRespuestas(request).enqueue(new Callback<KolbResponse>() {
            @Override
            public void onResponse(Call<KolbResponse> call, Response<KolbResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    KolbResponse resultado = response.body();

                    Log.d("KOLB_RES_BODY", "mensaje=" + resultado.getMensaje()
                            + " estiloDominante=" + resultado.getEstiloDominante());

                    // El POST no trae fecha → ponemos una local para mostrar algo en UI
                    String fechaEnviar = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(new Date());

                    Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                    intent.putExtra("estilo", resultado.getEstiloDominante());
                    intent.putExtra("caracteristicas", resultado.getCaracteristicas());
                    intent.putExtra("recomendaciones", resultado.getRecomendaciones());
                    intent.putExtra("fecha", fechaEnviar);
                    intent.putExtra("nombre", ""); // lo completará ResultActivity con GET
                    startActivity(intent);

                    Toast.makeText(TestActivity.this,
                            "Estilo: " + resultado.getEstiloDominante(),
                            Toast.LENGTH_LONG).show();

                } else {
                    try {
                        String errorMsg = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        Toast.makeText(TestActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("KOLB_ERROR", errorMsg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<KolbResponse> call, Throwable t) {
                Toast.makeText(TestActivity.this, "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("KOLB_FAIL", "onFailure: ", t);
            }
        });
    }
}