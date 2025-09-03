package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {

    private LinearLayout containerPreguntas;
    private Button btnEnviar;

    private List<PreguntasKolb> listaPreguntas = new ArrayList<>();
    private List<KolbRequest.Respuesta> listaRespuestas = new ArrayList<>();

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

        Call<List<PreguntasKolb>> call = api.getPreguntas();
        call.enqueue(new Callback<List<PreguntasKolb>>() {
            @Override
            public void onResponse(Call<List<PreguntasKolb>> call, Response<List<PreguntasKolb>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPreguntas = response.body();
                    Collections.reverse(listaPreguntas);
                    mostrarPreguntas();
                }
            }

            @Override
            public void onFailure(Call<List<PreguntasKolb>> call, Throwable t) {
                Log.e("Kolb", "Error cargando preguntas", t);
            }
        });
    }

    private void mostrarPreguntas() {
        containerPreguntas.removeAllViews();

        // Ordenar ASCENDENTE por ID
        Collections.sort(listaPreguntas, (p1, p2) ->
                Integer.compare(p1.getId_pregunta_estilo_aprendizajes(),
                        p2.getId_pregunta_estilo_aprendizajes())
        );


        for (PreguntasKolb p : listaPreguntas) {
            // Tipo de pregunta
            TextView tvTipo = new TextView(this);
            tvTipo.setText(p.getTipo_pregunta());
            tvTipo.setTextSize(12f);
            tvTipo.setPadding(0, 16, 0, 4);

            // Título
            TextView tvTitulo = new TextView(this);
            tvTitulo.setText(p.getTitulo());
            tvTitulo.setTextSize(16f);
            tvTitulo.setTextAppearance(this, android.R.style.TextAppearance_Medium);

            // Pregunta
            TextView tvPregunta = new TextView(this);
            tvPregunta.setText(p.getPregunta());
            tvPregunta.setTextSize(14f);
            tvPregunta.setPadding(0, 0, 0, 8);

            // Opciones
            RadioGroup rgOpciones = new RadioGroup(this);
            rgOpciones.setOrientation(RadioGroup.HORIZONTAL);

            for (int i = 1; i <= 4; i++) {
                RadioButton rb = new RadioButton(this);
                rb.setText(String.valueOf(i));   // lo que ve el usuario
                rb.setTag(i);                    // lo que guardas realmente
                rgOpciones.addView(rb);
            }

            // Guardar respuesta seleccionada
            rgOpciones.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton seleccionado = group.findViewById(checkedId);

                if (seleccionado != null) {
                    int valor = (int) seleccionado.getTag(); // usamos el tag, no el texto

                    boolean encontrada = false;
                    for (KolbRequest.Respuesta r : listaRespuestas) {
                        if (r.getId_pregunta() == p.getId_pregunta_estilo_aprendizajes()) {
                            r.setValor(valor);
                            encontrada = true;
                            break;
                        }
                    }

                    if (!encontrada) {
                        listaRespuestas.add(new KolbRequest.Respuesta(
                                p.getId_pregunta_estilo_aprendizajes(),
                                valor
                        ));
                    }
                }
            });

            // Agregar al contenedor
            containerPreguntas.addView(tvTipo);
            containerPreguntas.addView(tvTitulo);
            containerPreguntas.addView(tvPregunta);
            containerPreguntas.addView(rgOpciones);
        }
    }

    private void enviarRespuestas() {
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);

        int userId = TokenManager.getUserId(this);
        KolbRequest request = new KolbRequest(userId, listaRespuestas);


        Call<KolbResponse> call = api.guardarRespuestas(request);

        call.enqueue(new Callback<KolbResponse>() {
            @Override
            public void onResponse(Call<KolbResponse> call, Response<KolbResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    KolbResponse resultado = response.body();

                    Toast.makeText(TestActivity.this, "Estilo: " + resultado.getEstiloDominante(), Toast.LENGTH_LONG).show();

                    // Pasar solo lo necesario como Strings
                    Intent intent = new Intent(TestActivity.this, ResultActivity.class);
                    intent.putExtra("estilo", resultado.getEstiloDominante());
                    intent.putExtra("caracteristicas", resultado.getCaracteristicas());
                    intent.putExtra("recomendaciones", resultado.getRecomendaciones());
                    intent.putExtra("fecha", resultado.getFecha());
                    intent.putExtra("nombre", resultado.getNombre() + " " + resultado.getApellido());
                    startActivity(intent);

                } else {
                    Toast.makeText(TestActivity.this, "Error enviando respuestas", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<KolbResponse> call, Throwable t) {
                Log.e("Kolb", "Error guardando respuestas", t);
            }
        });
    }

}