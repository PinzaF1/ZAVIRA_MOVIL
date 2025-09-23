package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.ParadaResponse;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.model.SimulacroRequest;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Simulacro final (25 preguntas, 5 por subtema). Sin estilo Kolb. */
public class FinalSimulacroActivity extends AppCompatActivity {

    public static final String EXTRA_AREA = "extra_area";
    public static final String EXTRA_SUBTEMAS = "extra_subtemas"; // ArrayList<String>

    private ActivityQuizBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;

    private String areaUi;
    private ArrayList<String> subtemas;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        areaUi = getIntent().getStringExtra(EXTRA_AREA);
        subtemas = getIntent().getStringArrayListExtra(EXTRA_SUBTEMAS);
        if (subtemas == null) subtemas = new ArrayList<>();

        binding.tvAreaSubtema.setText((areaUi != null ? areaUi : "") + " • Simulacro final");

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>());
        binding.rvQuestions.setAdapter(adapter);

        binding.btnEnviar.setOnClickListener(v -> enviar());

        crearSimulacroYMostrar();
    }

    private void setLoading(boolean b) {
        binding.progress.setVisibility(b ? View.VISIBLE : View.GONE);
        binding.btnEnviar.setEnabled(!b);
    }

    private void crearSimulacroYMostrar() {
        setLoading(true);

        final String areaApi = AreaMapper.toApiArea(areaUi);
        // subtemas ya vienen en UI, normalizamos un poco (opcional)
        ArrayList<String> subsApi = new ArrayList<>();
        for (String s : subtemas) subsApi.add(AreaMapper.normalizeSubtema(s));

        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        SimulacroRequest req = new SimulacroRequest(areaApi, subsApi);

        api.crearSimulacro(req).enqueue(new Callback<ParadaResponse>() {
            @Override public void onResponse(Call<ParadaResponse> call, Response<ParadaResponse> resp) {
                setLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(FinalSimulacroActivity.this, "No se pudo crear el simulacro (HTTP " + resp.code() + ")", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                ParadaResponse pr = resp.body();
                idSesion = (pr.sesion != null) ? pr.sesion.id_sesion : null;

                ArrayList<Question> preguntas = new ArrayList<>();
                if (pr.preguntas != null) preguntas.addAll(pr.preguntas);

                // solo nos aseguramos de máximo 25 (5x5)
                if (preguntas.size() > 25) preguntas = new ArrayList<>(preguntas.subList(0, 25));
                if (preguntas.isEmpty()) {
                    Toast.makeText(FinalSimulacroActivity.this, "No hay preguntas para el simulacro.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                adapter = new QuizQuestionsAdapter(preguntas);
                binding.rvQuestions.setAdapter(adapter);
            }

            @Override public void onFailure(Call<ParadaResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(FinalSimulacroActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void enviar() {
        if (adapter.getItemCount() == 0) {
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

        if (idSesion == null) {
            Toast.makeText(this, "No hay sesión activa.", Toast.LENGTH_LONG).show();
            return;
        }

        setLoading(true);
        ApiService api = RetrofitClient.getInstance(this).create(ApiService.class);
        api.cerrarSesion(new CerrarRequest(idSesion, rs)).enqueue(new Callback<CerrarResponse>() {
            @Override public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(FinalSimulacroActivity.this, "No se pudo cerrar el simulacro.", Toast.LENGTH_LONG).show();
                    return;
                }
                CerrarResponse r = response.body();
                Integer puntaje = r.puntaje;
                Toast.makeText(FinalSimulacroActivity.this, "Puntaje simulacro: " + (puntaje != null ? puntaje : 0) + "%", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override public void onFailure(Call<CerrarResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(FinalSimulacroActivity.this, "Fallo al cerrar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
