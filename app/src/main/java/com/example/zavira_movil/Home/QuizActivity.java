package com.example.zavira_movil.Home;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.local.ProgressLockManager;
import com.example.zavira_movil.model.CerrarRequest;
import com.example.zavira_movil.model.CerrarResponse;
import com.example.zavira_movil.model.ParadaRequest;
import com.example.zavira_movil.model.ParadaResponse;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Quiz: crea sesión, carga 5 preguntas, cierra y actualiza desbloqueo/retroceso. */
public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_AREA    = "extra_area";
    public static final String EXTRA_SUBTEMA = "extra_subtema";
    public static final String EXTRA_NIVEL   = "extra_nivel"; // 1..5

    private ActivityQuizBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;

    private String areaUi, subtemaUi;
    private int nivel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        areaUi    = getIntent().getStringExtra(EXTRA_AREA);
        subtemaUi = getIntent().getStringExtra(EXTRA_SUBTEMA);
        nivel     = getIntent().getIntExtra(EXTRA_NIVEL, 1);

        binding.tvAreaSubtema.setText((areaUi != null ? areaUi : "") + " • " + (subtemaUi != null ? subtemaUi : ""));

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>());
        binding.rvQuestions.setAdapter(adapter);

        binding.btnEnviar.setOnClickListener(v -> enviar());

        crearParadaYMostrar();
    }

    private void setLoading(boolean b) {
        binding.progress.setVisibility(b ? View.VISIBLE : View.GONE);
        binding.btnEnviar.setEnabled(!b);
    }

    /** Crea sesión y pinta preguntas (máximo 5). */
    private void crearParadaYMostrar() {
        setLoading(true);

        final String areaApi    = AreaMapper.toApiArea(areaUi);
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
            @Override
            public void onResponse(Call<ParadaResponse> call, Response<ParadaResponse> resp) {
                setLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    Toast.makeText(QuizActivity.this, "No se pudo crear la sesión (HTTP " + resp.code() + ")", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                ParadaResponse pr = resp.body();
                idSesion = (pr.sesion != null) ? pr.sesion.id_sesion : null;

                ArrayList<Question> preguntas = new ArrayList<>();
                if (pr.preguntas != null) preguntas.addAll(pr.preguntas);

                // Por requisito: 5 preguntas por subtema
                if (preguntas.size() > 5) preguntas = new ArrayList<>(preguntas.subList(0, 5));
                if (preguntas.isEmpty()) {
                    Toast.makeText(QuizActivity.this, "No hay preguntas para este subtema.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                adapter = new QuizQuestionsAdapter(preguntas);
                binding.rvQuestions.setAdapter(adapter);
            }

            @Override
            public void onFailure(Call<ParadaResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /** Envía respuestas a /sesion/cerrar y maneja avance/retroceso. */
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
            @Override
            public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                setLoading(false);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(QuizActivity.this, "No se pudo cerrar la sesión.", Toast.LENGTH_LONG).show();
                    return;
                }
                CerrarResponse r = response.body();
                int puntaje = (r.puntaje != null) ? r.puntaje : 0;
                int correctas = (r.correctas != null) ? r.correctas : (puntaje / 20); // 5 preg = 20% c/u
                int incorrectas = 5 - correctas;

                Toast.makeText(QuizActivity.this, "Puntaje: " + puntaje + "%", Toast.LENGTH_LONG).show();

                if (correctas == 4) {
                    // 100% -> desbloquea siguiente subtema
                    ProgressLockManager.unlockNext(QuizActivity.this, areaUi, nivel);
                } else if (incorrectas >= 3 && nivel > 1) {
                    // 3 o más errores -> retroceso UN subtema (si no es el primero)
                    ProgressLockManager.setUnlockedLevel(QuizActivity.this, areaUi, Math.max(1, nivel - 1));
                    Toast.makeText(QuizActivity.this, "Retrocedes al subtema anterior por 3 errores.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(QuizActivity.this, "No apruebas aún. Intenta de nuevo.", Toast.LENGTH_LONG).show();
                }

                finish(); // Al volver, Home/SubjectAdapter refrescan y verás los cambios
            }

            @Override
            public void onFailure(Call<CerrarResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(QuizActivity.this, "Fallo al cerrar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
