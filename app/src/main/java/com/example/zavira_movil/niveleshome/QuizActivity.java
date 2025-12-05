package com.example.zavira_movil.niveleshome;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.QuizQuestionsAdapter;
import com.example.zavira_movil.R;
import com.example.zavira_movil.databinding.ActivityQuizBinding;
import com.example.zavira_movil.model.Question;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.example.zavira_movil.BasicResponse; // <-- import añadido para Callback<BasicResponse>
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;
import android.os.Looper;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Crea sesión, carga máx 10 preguntas, envía y desbloquea/retrocede según puntaje. */
public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_AREA    = "extra_area";     // área visible UI
    public static final String EXTRA_SUBTEMA = "extra_subtema";  // subtema visible UI
    public static final String EXTRA_NIVEL   = "extra_nivel";    // 1..5

    private ActivityQuizBinding binding;
    private QuizQuestionsAdapter adapter;
    private Integer idSesion;

    private String areaUi, subtemaUi; // usamos UI para ProgressLockManager
    private int nivel;
    private int currentQuestionIndex = 0; // Índice de la pregunta actual
    private List<Question> allQuestions = new ArrayList<>(); // Todas las preguntas
    private List<String> todasLasRespuestas = new ArrayList<>(); // Respuestas guardadas mientras avanza

    // Sistema de vidas
    private Handler handlerVidas;
    private Runnable runnableVidas;
    private static final long INTERVALO_ACTUALIZACION_VIDAS = 1000L; // Actualizar cada segundo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Asegurar fondo blanco y sin bordes
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }

        areaUi    = getIntent().getStringExtra(EXTRA_AREA);
        subtemaUi = getIntent().getStringExtra(EXTRA_SUBTEMA);
        nivel     = getIntent().getIntExtra(EXTRA_NIVEL, 1);

        // Configurar header
        binding.tvAreaSubtema.setText("Pregunta 1 de 5 • " + (areaUi != null ? areaUi : ""));

        binding.rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QuizQuestionsAdapter(new ArrayList<>(), areaUi);
        binding.rvQuestions.setAdapter(adapter);

        // Ocultar ProgressBar completamente desde el inicio - NO mostrar pantalla de carga
        if (binding.progress != null) {
            binding.progress.setVisibility(View.GONE);
        }

        // Configurar botón con color del área (igual que la barra de progreso)
        int areaColor = obtenerColorArea(areaUi);
        binding.btnEnviar.setBackgroundResource(R.drawable.bg_button_area_color);
        binding.btnEnviar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        binding.btnEnviar.setTextColor(Color.WHITE);
        binding.btnEnviar.setText("Siguiente Pregunta");
        binding.btnEnviar.setElevation(4f);

        binding.btnEnviar.setOnClickListener(v -> siguientePregunta());

        // Inicializar sistema de vidas (solo para niveles 2+)
        if (nivel > 1) {
            inicializarSistemaVidas();
        }

        crearParadaYMostrar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si volvemos de ver el detalle, intentar recargar media vida
        if (nivel > 1) {
            verificarRecargaPorDetalle();
            actualizarVidas();
            iniciarActualizacionVidas();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerActualizacionVidas();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerActualizacionVidas();

        // Log para diagnosticar salidas sin completar
        if (idSesion != null && currentQuestionIndex < allQuestions.size() - 1) {
            // Contar respuestas guardadas localmente (sin usar stream)
            int respuestasGuardadas = 0;
            for (String r : todasLasRespuestas) {
                if (r != null) respuestasGuardadas++;
            }

            android.util.Log.w("QuizActivity", "⚠️ USUARIO SALIÓ SIN COMPLETAR EL QUIZ");
            android.util.Log.w("QuizActivity", "  • idSesion: " + idSesion);
            android.util.Log.w("QuizActivity", "  • Pregunta actual: " + (currentQuestionIndex + 1) + "/" + allQuestions.size());
            android.util.Log.w("QuizActivity", "  • Respuestas guardadas localmente: " +
                respuestasGuardadas + "/" + todasLasRespuestas.size());
            android.util.Log.w("QuizActivity", "  📝 Estas respuestas NO fueron enviadas al backend");
            android.util.Log.w("QuizActivity", "  📝 Por eso el historial mostrará correctas=0, incorrectas=0");
        } else if (idSesion != null && currentQuestionIndex == allQuestions.size() - 1) {
            // Usuario estaba en la última pregunta
            List<String> marcadas = adapter != null ? adapter.getMarcadas() : new ArrayList<>();
            if (marcadas.isEmpty() || marcadas.get(0) == null) {
                android.util.Log.w("QuizActivity", "⚠️ USUARIO EN ÚLTIMA PREGUNTA PERO NO LA RESPONDIÓ");
                android.util.Log.w("QuizActivity", "  • idSesion: " + idSesion);
                android.util.Log.w("QuizActivity", "  • Salió sin hacer clic en 'Enviar'");
            }
        }
    }

    private void crearParadaYMostrar() {
        // Verificar sistema de vidas antes de crear sesión (solo niveles 2+)
        if (nivel > 1) {
            int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
            if (userIdInt > 0) {
                String userId = String.valueOf(userIdInt);
                int vidas = LivesManager.getLivesWithAutoRecharge(this, userId, areaUi, nivel);
                float partialLives = LivesManager.getPartialLives(this, userId, areaUi, nivel);

                // Solo bloquear si tiene la última vida en la mitad (vidas == 0 y partialLives > 0)
                if (vidas == 0 && partialLives > 0) {
                    // Mostrar diálogo emergente indicando que debe esperar
                    mostrarDialogoEsperarMediaVida(userId);
                    return;
                }
            }
        }

        setLoading(true);

        final String areaApi    = MapeadorArea.toApiArea(areaUi);
        final String subtemaApi = MapeadorArea.normalizeSubtema(subtemaUi);

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        ParadaRequest req = new ParadaRequest(
                areaApi != null ? areaApi : "",
                subtemaApi != null ? subtemaApi : "",
                Math.max(1, Math.min(5, nivel)),
                true,
                1
        );

        // 🔍 LOG DETALLADO DEL REQUEST
        android.util.Log.e("QuizActivity", "========================================");
        android.util.Log.e("QuizActivity", "📤 ENVIANDO REQUEST AL BACKEND");
        android.util.Log.e("QuizActivity", "========================================");
        android.util.Log.e("QuizActivity", "🌐 Endpoint: POST /sesion/parada");
        android.util.Log.e("QuizActivity", "📋 Parámetros enviados:");
        android.util.Log.e("QuizActivity", "  • area: '" + req.area + "' (UI: " + areaUi + ")");
        android.util.Log.e("QuizActivity", "  • subtema: '" + req.subtema + "' (UI: " + subtemaUi + ")");
        android.util.Log.e("QuizActivity", "  • nivel_orden: " + req.nivelOrden);
        android.util.Log.e("QuizActivity", "  • usa_estilo_kolb: " + req.usaEstiloKolb);
        android.util.Log.e("QuizActivity", "  • intento_actual: " + req.intentoActual);
        android.util.Log.e("QuizActivity", "========================================");
        android.util.Log.e("QuizActivity", "🤖 EXPECTATIVA: Backend debería decidir automáticamente:");
        android.util.Log.e("QuizActivity", "  • ✅ Usar OpenAI/IA para preguntas personalizadas");
        android.util.Log.e("QuizActivity", "  • ❌ O usar banco local como fallback");
        android.util.Log.e("QuizActivity", "  • 🔍 Respuesta debería tener id_pregunta=null si es IA");
        android.util.Log.e("QuizActivity", "========================================");

        logIAEvent("Solicitando preguntas a la API - Backend decide entre IA/OpenAI o banco local", idSesion, areaApi, subtemaApi, nivel, 0);

        api.crearParada(req).enqueue(new Callback<ParadaResponse>() {
            @Override public void onResponse(Call<ParadaResponse> call, Response<ParadaResponse> resp) {
                // Ocultar loading
                if (binding != null && binding.progress != null) {
                    binding.progress.setVisibility(View.GONE);
                }

                // 🔍 LOG DETALLADO DE LA RESPUESTA HTTP
                android.util.Log.e("QuizActivity", "========================================");
                android.util.Log.e("QuizActivity", "📥 RESPUESTA DEL BACKEND RECIBIDA");
                android.util.Log.e("QuizActivity", "========================================");
                android.util.Log.e("QuizActivity", "📊 HTTP Status: " + resp.code());
                android.util.Log.e("QuizActivity", "🌐 URL: " + call.request().url());
                android.util.Log.e("QuizActivity", "⏱️ Tiempo de respuesta: " + (System.currentTimeMillis() - System.currentTimeMillis()) + "ms");

                if (!resp.isSuccessful()) {
                    logIAEvent("Fallo al solicitar preguntas a la API (HTTP " + resp.code() + ")", idSesion, areaApi, subtemaApi, nivel, 0);

                    // Usar ErrorHandler para mostrar error con opción de reintentar
                    com.example.zavira_movil.utils.ErrorHandler.handleHttpError(
                            QuizActivity.this,
                            resp,
                            () -> crearParadaYMostrar() // Callback para reintentar
                    );
                    return;
                }

                ParadaResponse pr = resp.body();
                if (pr == null) {
                    logIAEvent("Respuesta sin cuerpo JSON de la API", idSesion, areaApi, subtemaUi, nivel, 0);

                    // Error de servidor sin cuerpo
                    com.example.zavira_movil.utils.ErrorHandler.ErrorInfo errorInfo =
                            new com.example.zavira_movil.utils.ErrorHandler.ErrorInfo(
                                    com.example.zavira_movil.utils.ErrorHandler.ErrorType.SERVER_ERROR,
                                    "Error del Servidor",
                                    "El servidor respondió sin contenido. Por favor, intenta más tarde.",
                                    "HTTP " + resp.code() + " sin body",
                                    true,
                                    resp.code()
                            );
                    com.example.zavira_movil.utils.ErrorHandler.showErrorDialog(
                            QuizActivity.this,
                            errorInfo,
                            () -> crearParadaYMostrar()
                    );
                    return;
                }

                if (pr.sesion != null) idSesion = pr.sesion.idSesion;

                ArrayList<ApiQuestion> apiQs = new ArrayList<>();
                if (pr.preguntas != null) apiQs.addAll(pr.preguntas);
                if (pr.preguntasPorSubtema != null) apiQs.addAll(pr.preguntasPorSubtema);
                if (pr.sesion != null) {
                    if (pr.sesion.preguntas != null) apiQs.addAll(pr.sesion.preguntas);
                    if (pr.sesion.preguntasPorSubtema != null) apiQs.addAll(pr.sesion.preguntasPorSubtema);
                }

                // 🔍 DIAGNÓSTICO DETALLADO: Verificar origen de las preguntas
                if (!apiQs.isEmpty()) {
                    // Examinar las primeras preguntas para determinar el origen
                    android.util.Log.e("QuizActivity", "========================================");
                    android.util.Log.e("QuizActivity", "🔍 DIAGNÓSTICO DE PREGUNTAS RECIBIDAS");
                    android.util.Log.e("QuizActivity", "========================================");
                    android.util.Log.e("QuizActivity", "📊 Total de preguntas recibidas: " + apiQs.size());

                    for (int i = 0; i < Math.min(3, apiQs.size()); i++) {
                        ApiQuestion q = apiQs.get(i);
                        android.util.Log.e("QuizActivity", "🔍 Pregunta #" + (i+1) + ":");
                        android.util.Log.e("QuizActivity", "  • id_pregunta: " + q.id_pregunta);
                        android.util.Log.e("QuizActivity", "  • texto: " + (q.enunciado != null ? q.enunciado.substring(0, Math.min(50, q.enunciado.length())) + "..." : "null"));
                        android.util.Log.e("QuizActivity", "  • opciones count: " + (q.opciones != null ? q.opciones.size() : 0));
                    }

                    boolean esIA = apiQs.get(0).id_pregunta == null;

                    // 🧠 ANÁLISIS ADICIONAL: Examinar contenido para confirmar origen
                    boolean contenidoPareceLaIA = analizarContenidoPreguntasIA(apiQs);

                    android.util.Log.e("QuizActivity", "========================================");
                    android.util.Log.e("QuizActivity", "🧪 ANÁLISIS DE CONTENIDO:");
                    android.util.Log.e("QuizActivity", "  • Contenido parece IA: " + contenidoPareceLaIA);
                    android.util.Log.e("QuizActivity", "  • id_pregunta es null: " + esIA);
                    android.util.Log.e("QuizActivity", "========================================");

                    if (esIA) {
                        android.util.Log.e("QuizActivity", "🤖 RESULTADO: PREGUNTAS GENERADAS POR IA/OPENAI");
                        android.util.Log.e("QuizActivity", "✅ id_pregunta es NULL → Preguntas de OpenAI");
                        if (contenidoPareceLaIA) {
                            android.util.Log.e("QuizActivity", "✅ Contenido confirma origen IA");
                        } else {
                            android.util.Log.w("QuizActivity", "⚠️ ALERTA: id_pregunta=null pero contenido no parece IA");
                        }
                        logIAEvent("🤖 ✅ PREGUNTAS GENERADAS CON OPENAI/IA", idSesion, areaApi, subtemaUi, nivel, apiQs.size());

                        // 🎯 MOSTRAR DIÁLOGO IA/ICFES cuando se detectan preguntas de IA (solo una vez por usuario)
                        ArrayList<Question> preguntasFinales = ApiQuestionMapper.toAppList(apiQs);
                        if (preguntasFinales.size() > 10) preguntasFinales = new ArrayList<>(preguntasFinales.subList(0, 10));

                        if (!preguntasFinales.isEmpty()) {
                            // Guardar preguntas
                            allQuestions = preguntasFinales;
                            currentQuestionIndex = 0;
                            todasLasRespuestas = new ArrayList<>();
                            for (int i = 0; i < preguntasFinales.size(); i++) {
                                todasLasRespuestas.add(null);
                            }

                            // Verificar si ya vio el diálogo IA/ICFES (solo mostrar una vez por UX)
                            int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(QuizActivity.this);
                            String prefsKey = "dialogo_ia_icfes_visto_" + userIdInt;
                            boolean yaVisto = getSharedPreferences("dialogo_ia_tutorial", MODE_PRIVATE).getBoolean(prefsKey, false);

                            if (!yaVisto) {
                                // Primera vez - mostrar diálogo informativo de IA/ICFES
                                mostrarDialogoIA_ICFES(areaUi, () -> {
                                    // Marcar como visto después de cerrar el diálogo
                                    getSharedPreferences("dialogo_ia_tutorial", MODE_PRIVATE)
                                            .edit()
                                            .putBoolean(prefsKey, true)
                                            .apply();

                                    // Después de cerrar el diálogo, mostrar la primera pregunta
                                    mostrarPreguntaActual();
                                });
                            } else {
                                // Ya vio el diálogo antes - ir directo a las preguntas
                                android.util.Log.d("QuizActivity", "🤖 Diálogo IA/ICFES ya visto por usuario " + userIdInt + " - saltando al quiz");
                                mostrarPreguntaActual();
                            }
                            return; // Salir aquí para no ejecutar el código de abajo
                        }
                    } else {
                        android.util.Log.e("QuizActivity", "📚 RESULTADO: PREGUNTAS DEL BANCO LOCAL");
                        android.util.Log.e("QuizActivity", "❌ id_pregunta=" + apiQs.get(0).id_pregunta + " → Banco de preguntas");
                        if (!contenidoPareceLaIA) {
                            android.util.Log.e("QuizActivity", "✅ Contenido confirma origen banco local");
                        } else {
                            android.util.Log.w("QuizActivity", "⚠️ ALERTA: Tiene id_pregunta pero contenido parece IA");
                        }
                        logIAEvent("📚 PREGUNTAS DEL BANCO LOCAL", idSesion, areaApi, subtemaUi, nivel, apiQs.size());

                        // REPORTAR AL BACKEND: indicar que estas preguntas NO fueron generadas por la API de IA
                        try {
                            java.util.ArrayList<com.example.zavira_movil.niveleshome.ReportIaRequest.ReportQuestion> rqList = new java.util.ArrayList<>();
                            for (int i = 0; i < apiQs.size() && i < 10; i++) {
                                ApiQuestion q = apiQs.get(i);
                                String preview = q.enunciado != null ? q.enunciado.substring(0, Math.min(200, q.enunciado.length())) : null;
                                boolean likelyIa = analizarContenidoPreguntasIA(java.util.Collections.singletonList(q));
                                rqList.add(new com.example.zavira_movil.niveleshome.ReportIaRequest.ReportQuestion(i + 1, q.id_pregunta, preview, likelyIa));
                            }

                            Integer userId = com.example.zavira_movil.local.TokenManager.getUserId(QuizActivity.this);
                            // Usar SimpleDateFormat para compatibilidad con API < 26
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                            sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                            String ts = sdf.format(new java.util.Date());
                            com.example.zavira_movil.niveleshome.ReportIaRequest report = new com.example.zavira_movil.niveleshome.ReportIaRequest(
                                    idSesion != null ? idSesion : null,
                                    userId != null && userId > 0 ? userId : null,
                                    areaApi,
                                    subtemaApi,
                                    nivel,
                                    "client-heuristic",
                                    false,
                                    "id_pregunta_present",
                                    rqList,
                                    ts
                            );

                            // Enviar en background (no bloquear UI)
                            ApiService apiForReport = RetrofitClient.getInstance().create(ApiService.class);
                            apiForReport.reportIaUsage(report).enqueue(new Callback<BasicResponse>() {
                                @Override
                                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                                    if (response.isSuccessful()) {
                                        android.util.Log.d("QuizActivity", "IA report enviado correctamente (200/2xx)");
                                    } else {
                                        android.util.Log.w("QuizActivity", "Fallo al enviar IA report: HTTP " + response.code());
                                    }
                                }

                                @Override
                                public void onFailure(Call<BasicResponse> call, Throwable t) {
                                    android.util.Log.w("QuizActivity", "Error enviando IA report: " + t.getMessage());
                                }
                            });
                        } catch (Exception ex) {
                            android.util.Log.w("QuizActivity", "No se pudo construir/enviar reporte IA: " + ex.getMessage());
                        }
                    }
                    android.util.Log.e("QuizActivity", "========================================");
                } else {
                    android.util.Log.e("QuizActivity", "⚠️ No se recibieron preguntas de la API");
                    logIAEvent("⚠️ No se recibieron preguntas de la API", idSesion, areaApi, subtemaUi, nivel, 0);
                }

                ArrayList<Question> preguntas = ApiQuestionMapper.toAppList(apiQs);
                if (preguntas.size() > 10) preguntas = new ArrayList<>(preguntas.subList(0, 10));
                if (preguntas.isEmpty()) {
                    Toast.makeText(QuizActivity.this, "No hay preguntas para este subtema.", Toast.LENGTH_LONG).show();
                    logIAEvent("No hay preguntas para este subtema", idSesion, areaApi, subtemaUi, nivel, 0);
                    finish();
                    return;
                }

                // Guardar todas las preguntas
                allQuestions = preguntas;
                currentQuestionIndex = 0;
                todasLasRespuestas = new ArrayList<>();
                // Inicializar lista de respuestas con nulls
                for (int i = 0; i < preguntas.size(); i++) {
                    todasLasRespuestas.add(null);
                }

                // Mostrar la primera pregunta
                mostrarPreguntaActual();
            }

            @Override public void onFailure(Call<ParadaResponse> call, Throwable t) {
                // Ocultar loading
                if (binding != null && binding.progress != null) {
                    binding.progress.setVisibility(View.GONE);
                }

                // Usar ErrorHandler para manejar excepción de red
                com.example.zavira_movil.utils.ErrorHandler.handleNetworkException(
                        QuizActivity.this,
                        t,
                        () -> crearParadaYMostrar() // Callback para reintentar
                );
            }
        });
    }

    /** Muestra la pregunta actual */
    private void mostrarPreguntaActual() {
        if (allQuestions.isEmpty() || currentQuestionIndex >= allQuestions.size()) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear lista con solo la pregunta actual para el adapter
        List<Question> preguntaActual = new ArrayList<>();
        preguntaActual.add(allQuestions.get(currentQuestionIndex));

        // Obtener la respuesta guardada para esta pregunta (si existe)
        String respuestaGuardada = todasLasRespuestas.get(currentQuestionIndex);

        // Crear adapter con la pregunta actual, respuesta guardada y número de pregunta
        adapter = new QuizQuestionsAdapter(preguntaActual, areaUi, respuestaGuardada, currentQuestionIndex + 1);
        binding.rvQuestions.setAdapter(adapter);

        // Actualizar header
        binding.tvAreaSubtema.setText("Pregunta " + (currentQuestionIndex + 1) + " de " + allQuestions.size() + " • " + (areaUi != null ? areaUi : ""));

        // Actualizar texto y color del botón (usar color del área)
        int areaColor = obtenerColorArea(areaUi);
        binding.btnEnviar.setBackgroundResource(R.drawable.bg_button_area_color);
        binding.btnEnviar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        if (currentQuestionIndex == allQuestions.size() - 1) {
            binding.btnEnviar.setText("Finalizar");
        } else {
            binding.btnEnviar.setText("Siguiente Pregunta");
        }
    }

    /** Avanza a la siguiente pregunta o envía todas las respuestas si es la última */
    private void siguientePregunta() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que la pregunta actual tenga respuesta
        List<String> marcadas = adapter.getMarcadas();
        if (marcadas.isEmpty() || marcadas.get(0) == null) {
            Toast.makeText(this, "Por favor selecciona una respuesta.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar la respuesta de la pregunta actual
        String respuestaActual = marcadas.get(0);
        todasLasRespuestas.set(currentQuestionIndex, respuestaActual);

        // Si es la última pregunta, enviar todas las respuestas
        if (currentQuestionIndex == allQuestions.size() - 1) {
            enviarTodasLasRespuestas();
        } else {
            // Avanzar a la siguiente pregunta
            currentQuestionIndex++;
            mostrarPreguntaActual();
        }
    }

    /** Envía todas las respuestas: intenta NUEVO y si falla con "cannot extract elements from an object", reintenta LEGACY. */
    private void enviarTodasLasRespuestas() {
        if (allQuestions.isEmpty()) {
            Toast.makeText(this, "No hay preguntas.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Asegurar que la última respuesta está guardada
        List<String> marcadasActual = adapter.getMarcadas();
        if (!marcadasActual.isEmpty() && marcadasActual.get(0) != null) {
            todasLasRespuestas.set(currentQuestionIndex, marcadasActual.get(0));
        }

        // Verificar que todas las preguntas tengan respuesta
        for (int i = 0; i < todasLasRespuestas.size(); i++) {
            if (todasLasRespuestas.get(i) == null) {
                Toast.makeText(this, "Por favor responde todas las preguntas.", Toast.LENGTH_SHORT).show();
                // Volver a la pregunta sin respuesta
                currentQuestionIndex = i;
                mostrarPreguntaActual();
                return;
            }
        }

        if (idSesion == null) {
            Toast.makeText(this, "No hay sesión activa.", Toast.LENGTH_LONG).show();
            return;
        }

        android.util.Log.d("QuizActivity", "========================================");
        android.util.Log.d("QuizActivity", "📤 ENVIANDO RESPUESTAS AL BACKEND");
        android.util.Log.d("QuizActivity", "========================================");
        android.util.Log.d("QuizActivity", "idSesion: " + idSesion);
        android.util.Log.d("QuizActivity", "Total preguntas: " + allQuestions.size());
        android.util.Log.d("QuizActivity", "Pregunta actual index: " + currentQuestionIndex);

        // Construir lista de respuestas
        List<CerrarRequest.Respuesta> rs = new ArrayList<>();
        for (int i = 0; i < todasLasRespuestas.size(); i++) {
            String respuesta = todasLasRespuestas.get(i);
            Integer idPregunta = null;

            // Obtener id_pregunta de la pregunta correspondiente
            if (i < allQuestions.size() && allQuestions.get(i).id_pregunta != null) {
                try {
                    idPregunta = Integer.parseInt(allQuestions.get(i).id_pregunta);
                } catch (NumberFormatException e) {
                    // id_pregunta es null o no es numérico, se mantiene como null
                }
            }

            rs.add(new CerrarRequest.Respuesta(i + 1, idPregunta, respuesta));
            android.util.Log.d("QuizActivity", "  • Respuesta " + (i+1) + ": orden=" + (i+1) +
                ", id_pregunta=" + idPregunta + ", opcion=" + respuesta);
        }

        android.util.Log.d("QuizActivity", "✅ Total respuestas a enviar: " + rs.size());
        android.util.Log.d("QuizActivity", "🌐 Enviando POST /sesion/cerrar");
        android.util.Log.d("QuizActivity", "========================================");

        // Mostrar loading
        if (binding != null && binding.progress != null) {
            binding.progress.setVisibility(View.VISIBLE);
        }

        CerrarRequest req = new CerrarRequest(idSesion, rs);
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);

        api.cerrarSesion(req).enqueue(new Callback<CerrarResponse>() {
            @Override
            public void onResponse(Call<CerrarResponse> call, Response<CerrarResponse> response) {
                // Ocultar loading
                if (binding != null && binding.progress != null) {
                    binding.progress.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null) {
                    onCierreOk(response.body());
                    return;
                }

                // ¿Mensaje típico del server legacy?
                String err = readErr(response.errorBody());
                boolean esLegacy = response.code() == 400 &&
                        err != null && err.contains("cannot extract elements from an object");

                if (esLegacy) {
                    // Reintento: LEGACY
                    android.util.Log.w("QuizActivity", "⚠️ Endpoint nuevo falló, reintentando con formato legacy...");
                    Map<String, Object> compat = new HashMap<>();
                    compat.put("id_sesion", idSesion);
                    List<List<Object>> legacyRs = new ArrayList<>();

                    for (CerrarRequest.Respuesta r : rs) {
                        List<Object> par = new ArrayList<>();
                        par.add(r.opcion);
                        par.add(r.orden);
                        legacyRs.add(par);
                    }
                    compat.put("respuestas", legacyRs);

                    api.cerrarSesionCompat(compat).enqueue(new Callback<CerrarResponse>() {
                        @Override
                        public void onResponse(Call<CerrarResponse> call2, Response<CerrarResponse> resp2) {
                            // Ocultar loading
                            if (binding != null && binding.progress != null) {
                                binding.progress.setVisibility(View.GONE);
                            }

                            if (resp2.isSuccessful() && resp2.body() != null) {
                                onCierreOk(resp2.body());
                            } else {
                                com.example.zavira_movil.utils.ErrorHandler.handleHttpError(
                                        QuizActivity.this,
                                        resp2,
                                        () -> enviarTodasLasRespuestas() // Reintentar envío
                                );
                            }
                        }

                        @Override
                        public void onFailure(Call<CerrarResponse> call2, Throwable t2) {
                            // Ocultar loading
                            if (binding != null && binding.progress != null) {
                                binding.progress.setVisibility(View.GONE);
                            }

                            com.example.zavira_movil.utils.ErrorHandler.handleNetworkException(
                                    QuizActivity.this,
                                    t2,
                                    () -> enviarTodasLasRespuestas() // Reintentar envío
                            );
                        }
                    });
                } else {
                    android.util.Log.d("QuizActivity", "📥 Respuesta recibida de /sesion/cerrar");
                    android.util.Log.d("QuizActivity", "  HTTP Code: " + response.code());
                    android.util.Log.d("QuizActivity", "  isSuccessful: " + response.isSuccessful());

                    com.example.zavira_movil.utils.ErrorHandler.handleHttpError(
                            QuizActivity.this,
                            response,
                            () -> enviarTodasLasRespuestas() // Reintentar envío
                    );
                }
            }

            @Override
            public void onFailure(Call<CerrarResponse> call, Throwable t) {
                // Ocultar loading
                if (binding != null && binding.progress != null) {
                    binding.progress.setVisibility(View.GONE);
                }

                com.example.zavira_movil.utils.ErrorHandler.handleNetworkException(
                        QuizActivity.this,
                        t,
                        () -> enviarTodasLasRespuestas() // Reintentar envío
                );
            }
        });
    }

    private void onCierreOk(CerrarResponse r) {
        // Ocultar loading
        if (binding != null && binding.progress != null) {
            binding.progress.setVisibility(View.GONE);
        }

        Integer puntaje = r.puntaje;
        int correctas = r.correctas != null ? r.correctas : 0;
        int totalPreguntas = allQuestions.size();

        // CRÍTICO: Usar TokenManager como fuente única de verdad
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("QuizActivity", "ERROR: userId inválido al cerrar sesión");
            finish();
            return;
        }
        String userId = String.valueOf(userIdInt);

        // Nivel 1: Pasa con 4 o 5 correctas (sin límite de intentos)
        if (nivel == 1) {
            if (correctas >= 4) {
                // Pasa al nivel 2
                ProgressLockManager.unlockNextAndSync(this, userId, areaUi, nivel);
                // Reiniciar vidas para el nivel 2
                LivesManager.resetLivesForNextLevelAndSync(this, userId, areaUi, 2);

                // Mostrar diálogo explicativo del sistema de vidas (solo la primera vez)
                mostrarDialogoExplicacionVidas(areaUi);
                return;
            } else {
                // No pasa - mostrar diálogo modal en el centro de la pantalla
                mostrarDialogoNivel1Fallido(correctas, puntaje != null ? puntaje : 0);
                return;
            }
        }

        // Niveles 2+: Lógica con vidas
        if (Boolean.TRUE.equals(r.aprueba)) {
            // Si aprueba nivel 5, desbloquea Examen Final; si no, avanza normal
            if (nivel >= 5 && (puntaje != null && puntaje >= 80)) {
                ProgressLockManager.unlockFinalExamAndSync(this, userId, areaUi);
                // Inicializar vidas para el examen final (nivel 6)
                LivesManager.resetLivesAndSync(this, userId, areaUi, 6);
                // Mostrar diálogo explicativo del examen final
                mostrarDialogoExplicacionExamenFinal(areaUi);
            } else {
                ProgressLockManager.unlockNextAndSync(this, userId, areaUi, nivel);
                // Reiniciar vidas para el siguiente nivel
                LivesManager.resetLivesForNextLevelAndSync(this, userId, areaUi, nivel + 1);
                mostrarDialogoExito("¡Felicitaciones! Pasaste al Nivel " + (nivel + 1), areaUi);
            }
            return;
        } else {
            // No pasó - manejar vidas
            // CRÍTICO: Solo consumir 1 vida por intento, no más
            // IMPORTANTE: Si las vidas no están inicializadas, inicializarlas primero
            int vidasRestantes = LivesManager.getLives(this, userId, areaUi, nivel);
            android.util.Log.d("QuizActivity", "Antes de consumir vida - vidasRestantes: " + vidasRestantes + ", nivel: " + nivel);

            if (vidasRestantes == -1) {
                // Si no están inicializadas, inicializar con MAX_LIVES
                LivesManager.resetLives(this, userId, areaUi, nivel);
                vidasRestantes = LivesManager.getLives(this, userId, areaUi, nivel);
                android.util.Log.d("QuizActivity", "Vidas inicializadas: " + vidasRestantes);
            }

            // CRÍTICO: Solo consumir 1 vida - el backend calculará las vidas correctamente al cerrar la sesión
            // NO sincronizar vidas aquí porque el backend ya las calculará correctamente
            boolean tieneVidas = LivesManager.consumeLife(this, userId, areaUi, nivel);
            int nuevasVidas = LivesManager.getLives(this, userId, areaUi, nivel);
            android.util.Log.d("QuizActivity", "Después de consumir vida - tieneVidas: " + tieneVidas + ", nuevasVidas: " + nuevasVidas);

            // Sincronizar vidas con backend DESPUÉS de consumir (solo para informar, el backend calculará correctamente)
            // Solo sincronizar si el nivel es mayor a 1 (nivel 1 no tiene vidas)
            if (nivel > 1) {
                com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                        .actualizarVidasEnBackend(this, userId, areaUi, nivel, nuevasVidas);
            }

            if (tieneVidas) {
                // Todavía tiene vidas - mostrar diálogo
                mostrarDialogoVidas(correctas, totalPreguntas, nuevasVidas, false);
            } else {
                // CRÍTICO: Se acabaron las vidas - retroceder INMEDIATAMENTE y bloquear el nivel
                android.util.Log.d("QuizActivity", "Vidas agotadas en nivel " + nivel + " - retrocediendo INMEDIATAMENTE");

                // Retroceder INMEDIATAMENTE al nivel anterior (esto bloquea el nivel actual)
                ProgressLockManager.retrocederPorFalloAndSync(this, userId, areaUi, nivel);

                // Obtener el nivel retrocedido (debe ser nivel - 1)
                int nivelRetrocedido = ProgressLockManager.getUnlockedLevel(this, userId, areaUi);
                android.util.Log.d("QuizActivity", "Nivel retrocedido a: " + nivelRetrocedido + " (desde nivel " + nivel + ")");

                // Reiniciar vidas para el nivel retrocedido (3 vidas nuevas)
                LivesManager.resetLivesAndSync(this, userId, areaUi, nivelRetrocedido);

                // Verificar que el nivel se bloqueó correctamente
                int nivelVerificado = ProgressLockManager.getUnlockedLevel(this, userId, areaUi);
                if (nivelVerificado != nivelRetrocedido) {
                    android.util.Log.e("QuizActivity", "ERROR: El nivel no se bloqueó correctamente. Esperado: " + nivelRetrocedido + ", Obtenido: " + nivelVerificado);
                } else {
                    android.util.Log.d("QuizActivity", "✓ Nivel bloqueado correctamente. Nivel actual desbloqueado: " + nivelVerificado);
                }

                mostrarDialogoVidas(correctas, totalPreguntas, 0, true);
            }
        }

        // CRÍTICO: Sincronizar desde el backend DESPUÉS de manejar vidas para asegurar consistencia
        // El backend ya calculó las vidas correctamente al cerrar la sesión
        // IMPORTANTE: Sincronizar inmediatamente para que el retroceso se refleje correctamente
        if (userIdInt > 0) {
            // Sincronizar inmediatamente para que el retroceso se refleje correctamente
            com.example.zavira_movil.sincronizacion.ProgresoSincronizador.getInstance()
                    .sincronizarDesdeBackend(QuizActivity.this, String.valueOf(userIdInt));
        }
    }

    @android.annotation.SuppressLint("ResourceType")
    private void mostrarDialogoNivel1Fallido(int correctas, int puntaje) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_nivel1_fallido, null);
        int areaColor = obtenerColorArea(areaUi);

        // Configurar color de la tarjeta del diálogo (si existe en el layout)
        // Nota: cardDialog puede no existir en versiones anteriores del layout
        try {
            // Intentar encontrar cardDialog - puede no existir
            android.view.View cardView = null;
            try {
                cardView = dialogView.findViewById(android.R.id.content);
                // Si el ID específico no existe, simplemente continuaremos
            } catch (Exception ignored) {
                // ID no existe - continuar sin error
            }

            if (cardView instanceof com.google.android.material.card.MaterialCardView) {
                com.google.android.material.card.MaterialCardView cardDialog = (com.google.android.material.card.MaterialCardView) cardView;
                cardDialog.setCardBackgroundColor(Color.WHITE);
                cardDialog.setStrokeColor(areaColor);
                cardDialog.setStrokeWidth(dp(3));
            }
        } catch (Exception e) {
            android.util.Log.w("QuizActivity", "cardDialog no encontrado o error al configurar", e);
        }

        // Configurar elementos
        ImageView ivIcono = dialogView.findViewById(R.id.ivIcono);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvCorrectas = dialogView.findViewById(R.id.tvCorrectas);
        TextView tvPuntaje = dialogView.findViewById(R.id.tvPuntaje);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        MaterialButton btnSalir = dialogView.findViewById(R.id.btnSalir);

        // Configurar icono (puedes usar un ícono de alerta o similar)
        ivIcono.setImageResource(android.R.drawable.ic_dialog_alert);
        ivIcono.setColorFilter(areaColor);

        // ... resto del código...

        // Configurar textos
        tvTitulo.setText("Necesitas Practicar Más");
        tvTitulo.setTextColor(Color.parseColor("#1F2937"));

        tvCorrectas.setText("Correctas: " + correctas + " de " + allQuestions.size());
        tvCorrectas.setTextColor(areaColor);

        tvPuntaje.setText("Puntaje: " + puntaje + "%");
        tvPuntaje.setTextColor(areaColor);

        tvMensaje.setText("Necesitas 4 o 5 correctas para pasar al siguiente nivel.\n\nPuedes intentarlo nuevamente sin límite.");
        tvMensaje.setTextColor(Color.parseColor("#666666"));

        // Configurar botones con color del área
        btnSalir.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CCCCCC")));

        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Configurar ventana del diálogo
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }


        // Botón Salir
        btnSalir.setOnClickListener(v -> {
            dialog.dismiss();
            setResult(RESULT_OK);
            finish();
        });

        dialog.show();
    }

    private void mostrarDialogoVidas(int correctas, int totalPreguntas, int vidasRestantes, boolean sinVidas) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vidas_nivel, null);

        // Obtener color del área
        int areaColor = obtenerColorArea(areaUi);

        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            // Usar un color muy claro del área como fondo de la tarjeta
            int colorClaro = Color.argb(20, Color.red(areaColor), Color.green(areaColor), Color.blue(areaColor));
            cardDialog.setCardBackgroundColor(colorClaro);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(2);
        }

        // Configurar elementos del diálogo
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvSubtitulo = dialogView.findViewById(R.id.tvSubtitulo);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        TextView tvVidasRestantes = dialogView.findViewById(R.id.tvVidasRestantes);
        TextView tvTiempoRecarga = dialogView.findViewById(R.id.tvTiempoRecarga);
        TextView tvRegeneracion = dialogView.findViewById(R.id.tvRegeneracion);
        TextView tvMensajeFinal = dialogView.findViewById(R.id.tvMensajeFinal);
        TextView tvMensajeDetalle = dialogView.findViewById(R.id.tvMensajeDetalle);
        MaterialButton btnVerDetalle = dialogView.findViewById(R.id.btnVerDetalle);
        MaterialButton btnUsarVida = dialogView.findViewById(R.id.btnUsarVida);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        // Obtener userId
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        String userId = userIdInt > 0 ? String.valueOf(userIdInt) : "";

        // Verificar si se puede recargar por detalle
        boolean puedeRecargarPorDetalle = nivel > 1 && LivesManager.puedeRecargarPorDetalle(this, userId, areaUi, nivel);

        // Configurar icono y título según el ejemplo
        if (sinVidas) {
            ivIcono.setImageResource(android.R.drawable.ic_menu_revert);
            ivIcono.setColorFilter(Color.parseColor("#E53935"));
            tvTitulo.setText("Sin Vidas Disponibles");
            tvTitulo.setTextColor(Color.parseColor("#1F2937")); // Título oscuro para mejor legibilidad
        } else {
            ivIcono.setImageResource(android.R.drawable.ic_menu_revert);
            ivIcono.setColorFilter(areaColor);
            tvTitulo.setText("Necesitas Practicar Más");
            tvTitulo.setTextColor(Color.parseColor("#1F2937")); // Título oscuro para mejor legibilidad
        }

        // Configurar subtítulo
        tvSubtitulo.setText("Obtuviste " + correctas + " de " + totalPreguntas + " respuestas correctas");

        // Configurar corazones - diseño mejorado según ejemplo (más pequeños)
        llCorazones.removeAllViews();
        for (int i = 0; i < 3; i++) {
            ImageView ivCorazon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(28), dp(28)
            );
            params.setMargins(dp(4), 0, dp(4), 0);
            ivCorazon.setLayoutParams(params);
            ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);

            if (i < vidasRestantes) {
                // Corazón lleno del color del área
                ivCorazon.setImageResource(R.drawable.ic_heart_filled);
                ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                // Corazón vacío
                ivCorazon.setImageResource(R.drawable.ic_heart_empty);
                ivCorazon.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);
            }
            llCorazones.addView(ivCorazon);
        }

        // Configurar vidas restantes y tiempo de recarga
        if (sinVidas) {
            tvVidasRestantes.setVisibility(View.GONE);
            tvTiempoRecarga.setVisibility(View.GONE);
            tvRegeneracion.setVisibility(View.VISIBLE);
            tvRegeneracion.setTextColor(areaColor); // Color del área para el mensaje de regeneración
            tvRegeneracion.setText("Sin vidas - Regeneran en 5 minutos");
            tvMensajeFinal.setVisibility(View.VISIBLE);
            tvMensajeFinal.setTextColor(areaColor); // Color del área para el mensaje final
            tvMensajeFinal.setText("Sin vidas disponibles. Las vidas se recargan cada 5 minutos.");
            tvMensajeDetalle.setVisibility(View.GONE);
            btnVerDetalle.setVisibility(View.GONE);
            btnUsarVida.setVisibility(View.GONE);
        } else {
            tvVidasRestantes.setText("Te quedan " + vidasRestantes + " vidas");
            tvVidasRestantes.setTextColor(areaColor); // Color del área para vidas restantes
            tvRegeneracion.setVisibility(View.GONE);
            tvMensajeFinal.setVisibility(View.GONE);

            // Mostrar tiempo de recarga
            long tiempoRestante = LivesManager.getTiempoRestanteRecarga(this, userId, areaUi, nivel);
            if (tiempoRestante > 0) {
                String tiempoFormateado = LivesManager.formatearTiempoRestante(tiempoRestante);
                tvTiempoRecarga.setText("La vida se recarga en " + tiempoFormateado);
                tvTiempoRecarga.setTextColor(areaColor);
                tvTiempoRecarga.setVisibility(View.VISIBLE);
            } else {
                tvTiempoRecarga.setVisibility(View.GONE);
            }

            // Mostrar opción de recarga por detalle si está disponible
            if (puedeRecargarPorDetalle) {
                tvMensajeDetalle.setVisibility(View.VISIBLE);
                tvMensajeDetalle.setText("¿Quieres ver el detalle para recargar media vida?");
                btnVerDetalle.setVisibility(View.VISIBLE);
                btnVerDetalle.setText("Ver Detalle (Recarga media vida)");
                btnVerDetalle.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
                btnVerDetalle.setIconResource(android.R.drawable.ic_menu_view);
            } else {
                tvMensajeDetalle.setVisibility(View.GONE);
                btnVerDetalle.setVisibility(View.GONE);
            }

            btnUsarVida.setText("Reintentar");
            btnUsarVida.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
            btnUsarVida.setIconResource(android.R.drawable.ic_menu_revert);
        }

        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Configurar ventana del diálogo: overlay oscuro para fondo y transparente para el diálogo
        if (dialog.getWindow() != null) {
            // Fondo oscuro semi-transparente para el overlay
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            // Asegurar que el diálogo tenga el tamaño correcto
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        // Botón para ver detalle (recarga media vida)
        btnVerDetalle.setOnClickListener(v -> {
            dialog.dismiss();
            // Ir al detalle del intento
            if (idSesion != null && idSesion > 0) {
                irAlDetalle(idSesion);
            } else {
                Toast.makeText(this, "No se pudo obtener el ID de la sesión", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });

        // Botón para usar vida (volver a intentar)
        btnUsarVida.setOnClickListener(v -> {
            dialog.dismiss();

            // IMPORTANTE: Si el usuario presiona "Reintentar", limpiar vidas parciales
            // y crear un nuevo timestamp para la vida vacía (5 minutos desde ahora)
            if (nivel > 1 && userIdInt > 0) {
                // Limpiar vidas parciales ANTES de reiniciar
                LivesManager.limpiarVidasParcialesYCrearTimestamp(this, userId, areaUi, nivel);
                android.util.Log.d("QuizActivity", "Vidas parciales limpiadas al presionar Reintentar");

                // Forzar actualización inmediata de vidas para mostrar vida vacía
                actualizarVidas();
            }

            // Reiniciar el quiz: limpiar estado y crear nueva sesión
            idSesion = null;
            allQuestions.clear();
            todasLasRespuestas.clear();
            currentQuestionIndex = 0;

            // Crear nueva sesión/parada para reiniciar el quiz
            crearParadaYMostrar();
        });

        // Botón cancelar
        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            notificarActualizacionHistorial(); // Notificar actualización del historial
            setResult(RESULT_OK); // Notificar que hubo cambios para actualizar la UI
            finish();
        });

        dialog.show();
    }

    /**
     * Muestra un diálogo emergente cuando el usuario intenta iniciar el quiz
     * pero tiene la última vida en la mitad (las otras están vacías).
     * Debe esperar a que se complete la media vida antes de poder intentar.
     */
    private void mostrarDialogoEsperarMediaVida(String userId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_vidas_nivel, null);

        // Obtener color del área
        int areaColor = obtenerColorArea(areaUi);

        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(dp(3));
        }

        // Configurar elementos del diálogo
        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvSubtitulo = dialogView.findViewById(R.id.tvSubtitulo);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        TextView tvVidasRestantes = dialogView.findViewById(R.id.tvVidasRestantes);
        TextView tvTiempoRecarga = dialogView.findViewById(R.id.tvTiempoRecarga);
        TextView tvRegeneracion = dialogView.findViewById(R.id.tvRegeneracion);
        TextView tvMensajeFinal = dialogView.findViewById(R.id.tvMensajeFinal);
        TextView tvMensajeDetalle = dialogView.findViewById(R.id.tvMensajeDetalle);
        MaterialButton btnVerDetalle = dialogView.findViewById(R.id.btnVerDetalle);
        MaterialButton btnUsarVida = dialogView.findViewById(R.id.btnUsarVida);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);

        // Configurar icono y título
        ivIcono.setImageResource(android.R.drawable.ic_menu_revert);
        ivIcono.setColorFilter(areaColor);
        tvTitulo.setText("Espera a que se Complete la Vida");
        tvTitulo.setTextColor(Color.parseColor("#1F2937"));

        // Configurar subtítulo
        tvSubtitulo.setText("Tienes una vida en la mitad. Debes esperar a que se complete antes de intentar.");

        // Configurar corazones: 2 vacías + 1 media vida
        llCorazones.removeAllViews();
        float partialLives = LivesManager.getPartialLives(this, userId, areaUi, nivel);
        for (int i = 0; i < 3; i++) {
            if (i == 0 && partialLives > 0) {
                // Primera vida: media vida
                android.widget.FrameLayout frameCorazon = new android.widget.FrameLayout(this);
                LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                        dp(28), dp(28)
                );
                frameParams.setMargins(dp(4), 0, dp(4), 0);
                frameCorazon.setLayoutParams(frameParams);

                // Corazón vacío de fondo
                ImageView ivCorazonVacio = new ImageView(this);
                android.widget.FrameLayout.LayoutParams paramsVacio = new android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                );
                ivCorazonVacio.setLayoutParams(paramsVacio);
                ivCorazonVacio.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivCorazonVacio.setImageResource(R.drawable.ic_heart_empty);
                ivCorazonVacio.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);

                // Corazón lleno (mitad inferior)
                ImageView ivCorazonLleno = new ImageView(this);
                android.widget.FrameLayout.LayoutParams paramsLleno = new android.widget.FrameLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                );
                ivCorazonLleno.setLayoutParams(paramsLleno);
                ivCorazonLleno.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivCorazonLleno.setImageResource(R.drawable.ic_heart_filled);
                ivCorazonLleno.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);

                ivCorazonLleno.setClipToOutline(true);
                final int heartSizePx = dp(28);
                ivCorazonLleno.setOutlineProvider(new android.view.ViewOutlineProvider() {
                    @Override
                    public void getOutline(android.view.View view, android.graphics.Outline outline) {
                        int width = view.getWidth() > 0 ? view.getWidth() : heartSizePx;
                        int height = view.getHeight() > 0 ? view.getHeight() : heartSizePx;
                        outline.setRect(0, height / 2, width, height);
                    }
                });

                frameCorazon.addView(ivCorazonVacio);
                frameCorazon.addView(ivCorazonLleno);
                llCorazones.addView(frameCorazon);
            } else {
                // Corazón vacío
                ImageView ivCorazon = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dp(28), dp(28)
                );
                params.setMargins(dp(4), 0, dp(4), 0);
                ivCorazon.setLayoutParams(params);
                ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivCorazon.setImageResource(R.drawable.ic_heart_empty);
                ivCorazon.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);
                llCorazones.addView(ivCorazon);
            }
        }

        // Configurar mensajes
        tvVidasRestantes.setVisibility(View.GONE);
        tvRegeneracion.setVisibility(View.GONE);
        tvMensajeFinal.setVisibility(View.GONE);
        tvMensajeDetalle.setVisibility(View.GONE);
        btnVerDetalle.setVisibility(View.GONE);
        btnUsarVida.setVisibility(View.GONE);

        // Mostrar tiempo de recarga
        long tiempoRestante = LivesManager.getTiempoRestanteRecarga(this, userId, areaUi, nivel);
        if (tiempoRestante > 0) {
            String tiempoFormateado = LivesManager.formatearTiempoRestante(tiempoRestante);
            tvTiempoRecarga.setText("La vida se completará en " + tiempoFormateado);
            tvTiempoRecarga.setTextColor(areaColor);
            tvTiempoRecarga.setVisibility(View.VISIBLE);
        } else {
            tvTiempoRecarga.setVisibility(View.GONE);
        }

        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Configurar ventana del diálogo
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        // Botón cancelar - cerrar la actividad
        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
            notificarActualizacionHistorial(); // Notificar actualización del historial
            setResult(RESULT_OK);
            finish();
        });

        dialog.show();
    }

    /**
     * Navega al detalle del simulacro para recargar media vida.
     * Recarga media vida ANTES de navegar al detalle.
     * Cierra QuizActivity y navega a HomeActivity con el fragment de detalle.
     */
    private void irAlDetalle(int idSesion) {
        // Obtener userId
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            Toast.makeText(this, "No se pudo obtener el ID del usuario", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            return;
        }
        String userId = String.valueOf(userIdInt);

        // Recargar media vida ANTES de navegar (solo si está disponible)
        boolean recargado = LivesManager.recargarPorDetalle(this, userId, areaUi, nivel);
        if (recargado) {
            android.util.Log.d("QuizActivity", "Media vida recargada por detalle antes de navegar");
            Toast.makeText(this, "¡Media vida recargada!", Toast.LENGTH_SHORT).show();
        }

        // Crear Intent para navegar a HomeActivity con el fragment de detalle
        Intent intent = new Intent(this, com.example.zavira_movil.Home.HomeActivity.class);
        intent.putExtra("action", "show_detalle");
        intent.putExtra("id_sesion", idSesion);
        intent.putExtra("materia", areaUi);
        intent.putExtra("nivel", nivel); // IMPORTANTE: Pasar nivel para la recarga
        intent.putExtra("initial_tab", 1); // Abrir en la pestaña "Preguntas"
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarDialogoExplicacionVidas(String area) {
        // Verificar si ya vio el tutorial
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("QuizActivity", "ERROR: userId inválido en mostrarDialogoExplicacionVidas");
            return;
        }
        int userId = userIdInt;
        String prefsKey = "vidas_tutorial_visto_" + userId + "_" + area;
        boolean yaVisto = getSharedPreferences("vidas_tutorial", MODE_PRIVATE).getBoolean(prefsKey, false);

        if (yaVisto) {
            // Si ya vio el tutorial, solo mostrar toast y cerrar
            Toast.makeText(this, "¡Felicitaciones! Pasaste al Nivel 2", Toast.LENGTH_LONG).show();
            notificarActualizacionHistorial(); // Notificar actualización del historial
            setResult(RESULT_OK);
            finish();
            return;
        }

        // Mostrar diálogo explicativo
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_explicacion_vidas, null);
        int areaColor = obtenerColorArea(area);

        // Configurar color de la tarjeta del diálogo - diseño más sutil
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            // Fondo blanco con borde sutil del color del área
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(dp(3));
        }

        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        MaterialButton btnEntendido = dialogView.findViewById(R.id.btnEntendido);

        // Configurar icono con color del área (ya está dentro del contenedor circular)
        if (ivIcono != null) {
            ivIcono.setImageResource(R.drawable.ic_heart_filled);
            ivIcono.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        // Configurar corazones (3 llenos) - tamaño más pequeño y elegante
        llCorazones.removeAllViews();
        for (int i = 0; i < 3; i++) {
            ImageView ivCorazon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(28), dp(28)
            );
            params.setMargins(dp(4), 0, dp(4), 0);
            ivCorazon.setLayoutParams(params);
            ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivCorazon.setImageResource(R.drawable.ic_heart_filled);
            ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
            llCorazones.addView(ivCorazon);
        }

        // Configurar botón con color del área
        btnEntendido.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));

        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Configurar ventana del diálogo: overlay oscuro para fondo y transparente para el diálogo
        if (dialog.getWindow() != null) {
            // Fondo oscuro semi-transparente para el overlay
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            // Asegurar que el diálogo tenga el tamaño correcto
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        btnEntendido.setOnClickListener(v -> {
            // Marcar como visto
            getSharedPreferences("vidas_tutorial", MODE_PRIVATE)
                    .edit()
                    .putBoolean(prefsKey, true)
                    .apply();

            dialog.dismiss();
            Toast.makeText(this, "¡Felicitaciones! Pasaste al Nivel 2", Toast.LENGTH_LONG).show();
            notificarActualizacionHistorial(); // Notificar actualización del historial
            setResult(RESULT_OK);
            finish();
        });

        dialog.show();
    }

    private void mostrarDialogoExito(String mensaje, String area) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_exito_nivel, null);
        int areaColor = obtenerColorArea(area);

        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(dp(3));
        }

        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        MaterialButton btnContinuar = dialogView.findViewById(R.id.btnContinuar);

        tvMensaje.setText(mensaje);

        // Configurar botón con color del área
        btnContinuar.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));

        // Crear y mostrar diálogo
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Configurar ventana del diálogo: overlay oscuro para fondo y transparente para el diálogo
        if (dialog.getWindow() != null) {
            // Fondo oscuro semi-transparente para el overlay
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            // Asegurar que el diálogo tenga el tamaño correcto
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        btnContinuar.setOnClickListener(v -> {
            dialog.dismiss();
            notificarActualizacionHistorial(); // Notificar actualización del historial
            setResult(RESULT_OK);
            finish();
        });

        dialog.show();
    }

    private void mostrarDialogoExplicacionExamenFinal(String area) {
        // Verificar si ya vio el tutorial
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("QuizActivity", "ERROR: userId inválido en mostrarDialogoExplicacionExamenFinal");
            return;
        }
        int userId = userIdInt;
        String prefsKey = "examen_final_tutorial_visto_" + userId + "_" + area;
        boolean yaVisto = getSharedPreferences("examen_final_tutorial", MODE_PRIVATE).getBoolean(prefsKey, false);

        if (yaVisto) {
            // Si ya vio el tutorial, solo mostrar toast y cerrar
            Toast.makeText(this, "¡Felicitaciones! Desbloqueaste el Examen Final", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
            return;
        }

        // Mostrar diálogo explicativo
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_explicacion_vidas, null);
        int areaColor = obtenerColorArea(area);

        // Configurar color de la tarjeta del diálogo
        com.google.android.material.card.MaterialCardView cardDialog = dialogView.findViewById(R.id.cardDialog);
        if (cardDialog != null) {
            cardDialog.setCardBackgroundColor(Color.WHITE);
            cardDialog.setStrokeColor(areaColor);
            cardDialog.setStrokeWidth(dp(3));
        }

        ImageView ivIcono = dialogView.findViewById(R.id.ivIconoVida);
        TextView tvTitulo = dialogView.findViewById(R.id.tvTitulo);
        TextView tvMensaje = dialogView.findViewById(R.id.tvMensaje);
        LinearLayout llCorazones = dialogView.findViewById(R.id.llCorazones);
        MaterialButton btnEntendido = dialogView.findViewById(R.id.btnEntendido);

        if (ivIcono != null) {
            ivIcono.setImageResource(android.R.drawable.ic_menu_info_details);
            ivIcono.setColorFilter(areaColor);
        }

        tvTitulo.setText("¡Examen Final Desbloqueado!");
        tvTitulo.setTextColor(Color.parseColor("#1F2937"));

        tvMensaje.setText("¡Felicitaciones! Has desbloqueado el Examen Final de " + area + ".\n\n" +
                "El Examen Final consiste en 25 preguntas de esta área.\n\n" +
                "Para aprobar, necesitas responder correctamente 20 de las 25 preguntas.\n\n" +
                "Tendrás 3 intentos (vidas) para aprobar el examen. Si pierdes los 3 intentos, " +
                "podrás intentarlo nuevamente después de un tiempo.\n\n" +
                "Si apruebas, obtendrás una insignia por tu excelente desempeño.");

        // Configurar corazones (vidas)
        llCorazones.removeAllViews();
        for (int i = 0; i < 3; i++) {
            ImageView ivCorazon = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(28), dp(28)
            );
            params.setMargins(dp(4), 0, dp(4), 0);
            ivCorazon.setLayoutParams(params);
            ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivCorazon.setImageResource(R.drawable.ic_heart_filled);
            ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
            llCorazones.addView(ivCorazon);
        }

        btnEntendido.setBackgroundTintList(android.content.res.ColorStateList.valueOf(areaColor));
        btnEntendido.setOnClickListener(v -> {
            // Marcar tutorial como visto
            getSharedPreferences("examen_final_tutorial", MODE_PRIVATE)
                    .edit()
                    .putBoolean(prefsKey, true)
                    .apply();

            Toast.makeText(this, "¡Felicitaciones! Desbloqueaste el Examen Final", Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_overlay_oscuro);
            dialog.getWindow().setLayout(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            );
        }

        dialog.show();
    }

    private int obtenerColorArea(String area) {
        if (area == null) return Color.parseColor("#B6B9C2");
        String a = area.toLowerCase().trim();

        // Isla del Conocimiento / Todas las áreas - Amarillo
        if (a.contains("conocimiento") || a.contains("isla") ||
                (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
            return ContextCompat.getColor(this, R.color.area_conocimiento);
        }

        if (a.contains("matem")) return ContextCompat.getColor(this, R.color.area_matematicas);
        if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica"))
            return ContextCompat.getColor(this, R.color.area_lenguaje);
        if (a.contains("social") || a.contains("ciudad"))
            return ContextCompat.getColor(this, R.color.area_sociales);
        if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim"))
            return ContextCompat.getColor(this, R.color.area_ciencias);
        if (a.contains("ingl"))
            return ContextCompat.getColor(this, R.color.area_ingles);

        return Color.parseColor("#B6B9C2");
    }

    private int obtenerColorAreaSoft(String area) {
        if (area == null) return Color.parseColor("#BA68C8");
        String a = area.toLowerCase().trim();

        if (a.contains("matem")) return ContextCompat.getColor(this, R.color.area_matematicas_soft);
        if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica"))
            return ContextCompat.getColor(this, R.color.area_lenguaje_soft);
        if (a.contains("social") || a.contains("ciudad"))
            return ContextCompat.getColor(this, R.color.area_sociales_soft);
        if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim"))
            return ContextCompat.getColor(this, R.color.area_ciencias_soft);
        if (a.contains("ingl"))
            return ContextCompat.getColor(this, R.color.area_ingles_soft);

        return Color.parseColor("#BA68C8");
    }

    private int dp(int px) {
        return (int) (px * getResources().getDisplayMetrics().density);
    }

    private static String readErr(ResponseBody eb) {
        if (eb == null) return null;
        try { return eb.string(); } catch (IOException ignored) { return null; }
    }

    /**
     * Log utilitario para eventos de IA y preguntas.
     */
    private void logIAEvent(String mensaje, Integer idSesion, String area, String subtema, int nivel, int cantidadPreguntas) {
        String logMsg = "[IA_EVENT] " + mensaje +
                " | idSesion=" + (idSesion != null ? idSesion : "null") +
                ", área=" + (area != null ? area : "null") +
                ", subtema=" + (subtema != null ? subtema : "null") +
                ", nivel=" + nivel +
                ", preguntas=" + cantidadPreguntas;
        android.util.Log.d("QuizActivity", logMsg);
    }

    /**
     * 🧠 ANÁLISIS DE CONTENIDO: Determina si las preguntas parecen generadas por IA
     * Analiza patrones típicos de preguntas generadas por OpenAI vs banco estático
     */
    private boolean analizarContenidoPreguntasIA(java.util.List<ApiQuestion> preguntas) {
        if (preguntas == null || preguntas.isEmpty()) return false;

        int indicadoresIA = 0;
        int totalPreguntas = Math.min(3, preguntas.size()); // Analizar máximo 3 preguntas

        for (int i = 0; i < totalPreguntas; i++) {
            ApiQuestion pregunta = preguntas.get(i);
            if (pregunta.enunciado == null) continue;

            String texto = pregunta.enunciado.toLowerCase();

            // 🔍 INDICADORES DE IA/OPENAI:
            // 1. Preguntas más elaboradas y contextualizadas
            if (texto.contains("considera") || texto.contains("analiza") ||
                    texto.contains("reflexiona") || texto.contains("evalúa")) {
                indicadoresIA++;
                android.util.Log.d("QuizActivity", "  ✅ Indicador IA: Vocabulario elaborado");
            }

            // 2. Preguntas con contexto narrativo
            if (texto.contains("situación") || texto.contains("contexto") ||
                    texto.contains("escenario") || texto.contains("ejemplo")) {
                indicadoresIA++;
                android.util.Log.d("QuizActivity", "  ✅ Indicador IA: Contexto narrativo");
            }

            // 3. Longitud típica de IA (más detalladas)
            if (texto.length() > 200) {
                indicadoresIA++;
                android.util.Log.d("QuizActivity", "  ✅ Indicador IA: Pregunta detallada (" + texto.length() + " chars)");
            }

            // 4. Estructura más natural y conversacional
            if (texto.contains("¿qué opinas") || texto.contains("¿cómo crees") ||
                    texto.contains("¿por qué piensas") || texto.contains("¿cuál sería")) {
                indicadoresIA++;
                android.util.Log.d("QuizActivity", "  ✅ Indicador IA: Lenguaje conversacional");
            }

            // 5. Referencias a aplicación práctica
            if (texto.contains("en la vida real") || texto.contains("en tu experiencia") ||
                    texto.contains("aplicarías") || texto.contains("utilizarías")) {
                indicadoresIA++;
                android.util.Log.d("QuizActivity", "  ✅ Indicador IA: Aplicación práctica");
            }

            android.util.Log.d("QuizActivity", "  📊 Pregunta #" + (i+1) + ": " +
                    (texto.length() > 100 ? texto.substring(0, 100) + "..." : texto));
        }

        // Si tiene 2 o más indicadores de IA en las preguntas analizadas, probablemente es IA
        boolean pareceIA = indicadoresIA >= 2;

        android.util.Log.d("QuizActivity", "  📊 Total indicadores IA: " + indicadoresIA + "/" + totalPreguntas);
        android.util.Log.d("QuizActivity", "  🧠 Conclusión: " + (pareceIA ? "PARECE IA" : "PARECE BANCO LOCAL"));

        return pareceIA;
    }

    // ========== Sistema de Vidas ==========

    /**
     * Inicializa el sistema de vidas (solo para niveles 2+).
     */
    private void inicializarSistemaVidas() {
        if (nivel <= 1) return;

        // Obtener userId
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            android.util.Log.e("QuizActivity", "ERROR: userId inválido en inicializarSistemaVidas");
            return;
        }
        String userId = String.valueOf(userIdInt);

        // Inicializar vidas si no están inicializadas
        int vidas = LivesManager.getLives(this, userId, areaUi, nivel);
        if (vidas == -1) {
            LivesManager.resetLives(this, userId, areaUi, nivel);
            vidas = LivesManager.getLives(this, userId, areaUi, nivel);
        }

        // Mostrar vidas en la pantalla
        actualizarVidas();

        // Iniciar actualización periódica
        iniciarActualizacionVidas();
    }

    /**
     * Actualiza la visualización de vidas en la pantalla.
     */
    private void actualizarVidas() {
        if (nivel <= 1) {
            // Ocultar vidas para nivel 1
            if (binding.llVidasContainer != null) {
                binding.llVidasContainer.setVisibility(View.GONE);
            }
            return;
        }

        // Obtener userId
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            return;
        }
        String userId = String.valueOf(userIdInt);

        // Obtener vidas con recarga automática
        int vidas = LivesManager.getLivesWithAutoRecharge(this, userId, areaUi, nivel);
        if (vidas == -1) {
            // No inicializado, ocultar vidas
            if (binding.llVidasContainer != null) {
                binding.llVidasContainer.setVisibility(View.GONE);
            }
            return;
        }

        // Obtener vidas parciales (media vida)
        float partialLives = LivesManager.getPartialLives(this, userId, areaUi, nivel);

        // Mostrar contenedor de vidas
        if (binding.llVidasContainer != null) {
            binding.llVidasContainer.setVisibility(View.VISIBLE);
        }

        // Obtener color del área
        int areaColor = obtenerColorArea(areaUi);

        // Limpiar corazones existentes
        LinearLayout llVidas = binding.llVidas;
        if (llVidas != null) {
            llVidas.removeAllViews();

            // Agregar corazones (3 máximo)
            for (int i = 0; i < 3; i++) {
                ImageView ivCorazon = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        dp(24), dp(24)
                );
                params.setMargins(dp(6), 0, 0, 0);
                ivCorazon.setLayoutParams(params);
                ivCorazon.setScaleType(ImageView.ScaleType.FIT_CENTER);

                if (i < vidas) {
                    // Corazón lleno del color del área
                    ivCorazon.setImageResource(R.drawable.ic_heart_filled);
                    ivCorazon.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    llVidas.addView(ivCorazon);
                } else if (i == vidas && partialLives > 0) {
                    // Media vida: mostrar corazón medio lleno
                    // La silueta del corazón vacío debe verse completa, solo la mitad inferior del corazón lleno debe estar visible
                    android.widget.FrameLayout frameCorazon = new android.widget.FrameLayout(this);
                    LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(
                            dp(24), dp(24)
                    );
                    frameParams.setMargins(dp(6), 0, 0, 0);
                    frameCorazon.setLayoutParams(frameParams);

                    // Corazón vacío de fondo (silueta completa visible)
                    ImageView ivCorazonVacio = new ImageView(this);
                    android.widget.FrameLayout.LayoutParams paramsVacio = new android.widget.FrameLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    ivCorazonVacio.setLayoutParams(paramsVacio);
                    ivCorazonVacio.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    ivCorazonVacio.setImageResource(R.drawable.ic_heart_empty);
                    ivCorazonVacio.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);

                    // Corazón lleno que solo se mostrará en la mitad inferior
                    ImageView ivCorazonLleno = new ImageView(this);
                    android.widget.FrameLayout.LayoutParams paramsLleno = new android.widget.FrameLayout.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    );
                    ivCorazonLleno.setLayoutParams(paramsLleno);
                    ivCorazonLleno.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    ivCorazonLleno.setImageResource(R.drawable.ic_heart_filled);
                    ivCorazonLleno.setColorFilter(areaColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    // Aplicar clip para mostrar solo la mitad inferior del corazón lleno
                    // Usar un ViewOutlineProvider estable que calcule el outline de forma consistente
                    // Esto evita el parpadeo al mantener el outline estable entre actualizaciones
                    ivCorazonLleno.setClipToOutline(true);
                    final int heartSizePx = dp(24);
                    final int halfHeartSizePx = heartSizePx / 2;
                    ivCorazonLleno.setOutlineProvider(new android.view.ViewOutlineProvider() {
                        @Override
                        public void getOutline(android.view.View view, android.graphics.Outline outline) {
                            // Usar dimensiones de la vista si están disponibles, sino usar valores calculados
                            int width = view.getWidth() > 0 ? view.getWidth() : heartSizePx;
                            int height = view.getHeight() > 0 ? view.getHeight() : heartSizePx;
                            // Clip para mostrar solo la mitad inferior
                            outline.setRect(0, height / 2, width, height);
                        }
                    });

                    // Agregar vistas: primero el vacío (fondo), luego el lleno (con clip)
                    frameCorazon.addView(ivCorazonVacio);
                    frameCorazon.addView(ivCorazonLleno);
                    llVidas.addView(frameCorazon);
                } else {
                    // Corazón vacío
                    ivCorazon.setImageResource(R.drawable.ic_heart_empty);
                    ivCorazon.setColorFilter(Color.parseColor("#CCCCCC"), android.graphics.PorterDuff.Mode.SRC_IN);
                    llVidas.addView(ivCorazon);
                }
            }
        }

        // Actualizar tiempo de recarga
        long tiempoRestante = LivesManager.getTiempoRestanteRecarga(this, userId, areaUi, nivel);
        TextView tvTiempoRecarga = binding.tvTiempoRecarga;
        if (tvTiempoRecarga != null) {
            if (tiempoRestante > 0 && (vidas < 3 || partialLives > 0)) {
                String tiempoFormateado = LivesManager.formatearTiempoRestante(tiempoRestante);
                tvTiempoRecarga.setText("Recarga en: " + tiempoFormateado);
                tvTiempoRecarga.setVisibility(View.VISIBLE);
            } else {
                tvTiempoRecarga.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Inicia la actualización periódica de vidas (cada segundo).
     */
    private void iniciarActualizacionVidas() {
        if (nivel <= 1) return;

        detenerActualizacionVidas();

        handlerVidas = new Handler(Looper.getMainLooper());
        runnableVidas = new Runnable() {
            @Override
            public void run() {
                actualizarVidas();
                // Programar siguiente actualización
                if (handlerVidas != null && runnableVidas != null) {
                    handlerVidas.postDelayed(runnableVidas, INTERVALO_ACTUALIZACION_VIDAS);
                }
            }
        };
        handlerVidas.post(runnableVidas);
    }

    /**
     * Detiene la actualización periódica de vidas.
     */
    private void detenerActualizacionVidas() {
        if (handlerVidas != null && runnableVidas != null) {
            handlerVidas.removeCallbacks(runnableVidas);
            runnableVidas = null;
        }
    }

    /**
     * Verifica si se puede recargar por detalle cuando vuelve de ver el detalle.
     */
    private void verificarRecargaPorDetalle() {
        if (nivel <= 1) return;

        // Obtener userId
        int userIdInt = com.example.zavira_movil.local.TokenManager.getUserId(this);
        if (userIdInt <= 0) {
            return;
        }
        String userId = String.valueOf(userIdInt);

        // Intentar recargar por detalle
        boolean recargado = LivesManager.recargarPorDetalle(this, userId, areaUi, nivel);
        if (recargado) {
            android.util.Log.d("QuizActivity", "Media vida recargada por detalle");
            // Mostrar mensaje
            Toast.makeText(this, "¡Media vida recargada por ver el detalle!", Toast.LENGTH_SHORT).show();
            // Actualizar vidas en la pantalla
            actualizarVidas();
        }
    }

    /**
     * Notifica la actualización del historial mediante broadcast
     */
    private void notificarActualizacionHistorial() {
        try {
            Intent intent = new Intent("com.example.zavira_movil.HISTORIAL_ACTUALIZADO");
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
        } catch (Exception e) {
            android.util.Log.e("QuizActivity", "Error al notificar actualización del historial", e);
        }
    }

    /**
     * Muestra/oculta el ProgressBar de carga
     */
    private void setLoading(boolean loading) {
        if (binding != null && binding.progress != null) {
            binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Muestra un diálogo informativo sobre IA ICFES
     */
    private void mostrarDialogoIA_ICFES(String area, Runnable onContinue) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("🤖 IA ICFES");
        builder.setMessage("Estamos utilizando inteligencia artificial para optimizar tus preguntas basadas en el área de " + area + ".");
        builder.setPositiveButton("Continuar", (dialog, which) -> {
            dialog.dismiss();
            if (onContinue != null) {
                onContinue.run();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}
