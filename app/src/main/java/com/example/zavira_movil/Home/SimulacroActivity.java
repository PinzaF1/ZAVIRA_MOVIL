package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.model.*;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Simulacro Final: 25 preguntas (5 por cada subtema del área). */
public class SimulacroActivity extends AppCompatActivity {

    private ActivityQuizBinding binding;
    private QuizQuestionsAdapter adapter;

    private String areaUi;
    private List<String> subtemas;   // 5 subtemas del área
    private int idxTanda = 0;        // 0..4
    private Integer idSesionActual;  // id de /crearParada
    private int correctasTotal = 0;  // 0..25

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        areaUi = getIntent().getStringExtra("AREA");
        if (areaUi == null) areaUi = "Matemáticas";

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>());
        binding.rvQuestions.setAdapter(adapter);

        subtemas = subtemasDe(areaUi);
        if (subtemas.isEmpty()) {
            Toast.makeText(this, "Área no soportada: " + areaUi, Toast.LENGTH_LONG).show();
            finish(); return;
        }

        binding.btnEnviar.setOnClickListener(v -> enviarTanda());

        binding.tvAreaSubtema.setText(areaUi + " • Simulacro (1/5)");
        crearParadaYMostrar(subtemas.get(idxTanda), idxTanda + 1);
    }

    private void setLoading(boolean b) {
        binding.progress.setVisibility(b ? View.VISIBLE : View.GONE);
        binding.btnEnviar.setEnabled(!b);
    }

    private void crearParadaYMostrar(String subtemaUi, int nivel) {
        setLoading(true);
        final String areaApi = AreaMapper.toApiArea(areaUi);
        final String subtemaApi = AreaMapper.normalizeSubtema(subtemaUi);

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        ParadaRequest req = new ParadaRequest(
                areaApi != null ? areaApi : "",
                subtemaApi != null ? subtemaApi : "",
                Math.max(1, Math.min(5, nivel)),
                true,
                1
        );

        api.crearParada(req).enqueue(new Callback<ParadaResponse>() {
            @Override public void onResponse(Call<ParadaResponse> call, Response<ParadaResponse> resp) {
                setLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(SimulacroActivity.this, "No se pudo crear la sesión (HTTP " + resp.code() + ")", Toast.LENGTH_LONG).show();
                    finish(); return;
                }
                ParadaResponse pr = resp.body();
                idSesionActual = (pr.sesion != null) ? pr.sesion.id_sesion : null;

                ArrayList<Question> preguntas = new ArrayList<>();
                if (pr.preguntas != null) preguntas.addAll(pr.preguntas);

                if (preguntas.size() > 5) preguntas = new ArrayList<>(preguntas.subList(0, 5));
                if (preguntas.isEmpty()) {
                    Toast.makeText(SimulacroActivity.this, "No hay preguntas para " + subtemaUi, Toast.LENGTH_LONG).show();
                    finish(); return;
                }

                adapter = new QuizQuestionsAdapter(preguntas);
                binding.rvQuestions.setAdapter(adapter);
            }

            @Override public void onFailure(Call<ParadaResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SimulacroActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void enviarTanda() {
        if (adapter == null || adapter.getItemCount() == 0) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> marcadas = adapter.getMarcadas();
        List<CerrarRequest.Respuesta> rs = new ArrayList<>();
        for (int i = 0; i < marcadas.size(); i++) {
            String k = marcadas.get(i);
            if (k == null) {
                Toast.makeText(this, "Responde todas las preguntas.", Toast.LENGTH_SHORT).show();
                return;
            }
            rs.add(new CerrarRequest.Respuesta(i + 1, k));
        }

        if (idSesionActual == null) {
            Toast.makeText(this, "No hay sesión activa.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.cerrarSesion(new CerrarRequest(idSesionActual, rs)).enqueue(new Callback<CerrarResponse>() {
            @Override public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(SimulacroActivity.this, "No se pudo cerrar la sesión.", Toast.LENGTH_LONG).show();
                    return;
                }
                CerrarResponse r = response.body();
                int puntaje   = (r.puntaje   != null) ? r.puntaje   : 0;
                int correctas = (r.correctas != null) ? r.correctas : Math.round(puntaje / 20f);
                correctasTotal += Math.max(0, Math.min(5, correctas));

                idxTanda++;
                if (idxTanda >= 5) {
                    binding.tvAreaSubtema.setText(areaUi + " • Simulacro (5/5)");
                    Toast.makeText(SimulacroActivity.this, (correctasTotal + " / 25 correctas"), Toast.LENGTH_LONG).show();
                    binding.btnEnviar.setEnabled(false);
                } else {
                    binding.tvAreaSubtema.setText(areaUi + " • Simulacro (" + (idxTanda + 1) + "/5)");
                    crearParadaYMostrar(subtemas.get(idxTanda), idxTanda + 1);
                }
            }

            @Override public void onFailure(Call<CerrarResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(SimulacroActivity.this, "Fallo al cerrar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private List<String> subtemasDe(String area) {
        switch (String.valueOf(area)) {
            case "Matemáticas":
                return Arrays.asList("Aritmética","Álgebra","Geometría","Estadística y Probabilidad","Funciones y Gráficas");
            case "Lectura crítica":
                return Arrays.asList("Comprensión lectora","Cohesión textual","Relaciones semánticas","Conectores lógicos","Propósito del autor");
            case "Sociales y ciudadanas":
                return Arrays.asList("Geografía","Historia","Economía","Ciudadanía","Pensamiento social");
            case "Ciencias naturales":
                return Arrays.asList("Biología","Química","Física","Ciencias de la Tierra","Método científico");
            case "Inglés":
                return Arrays.asList("Reading básico","Reading intermedio","Grammar y uso","Listening y contexto","Writing");
            default:
                return new ArrayList<>();
        }
    }
}
