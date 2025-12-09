package com.example.zavira_movil.detalleprogreso;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;

public class FragmentDetalleSimulacro extends Fragment {

    // ---- Vistas del header (usa tus IDs reales del layout) ----
    private TextView tvMateria, tvFecha, tvPuntaje, tvNivel, tvTiempo, tvCorr, tvInc;
    private ProgressBar progress;

    // ---- ViewPager + Adapter ----
    private DetalleSimuPagerAdapter pagerAdapter;

    // ---- Servicio local (no toca tu ApiService global) ----
    interface ProgresoService {
        @GET("movil/sesion/{id}/detalle")
        Call<ProgresoDetalleResponse> getDetalleSesion(@Path("id") int id);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detalle_simulacro, container, false);

        // Header
        tvMateria = v.findViewById(R.id.tvMateria);
        tvFecha   = v.findViewById(R.id.tvFecha);
        tvPuntaje = v.findViewById(R.id.tvPuntaje);
        tvNivel   = v.findViewById(R.id.tvNivel);
        tvTiempo  = v.findViewById(R.id.tvTiempo);
        tvCorr    = v.findViewById(R.id.tvCorrectas);
        tvInc     = v.findViewById(R.id.tvIncorrectas);
        progress  = v.findViewById(R.id.progress);

        // Pager + Tabs
        ViewPager2 pager = v.findViewById(R.id.viewPager);
        pagerAdapter = new DetalleSimuPagerAdapter(requireActivity());
        pager.setAdapter(pagerAdapter);

        TabLayout tabs = v.findViewById(R.id.tabLayout);
        // El TabLayout ya está configurado en el XML con fondo azul oscuro y texto blanco
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            if (position == 0) tab.setText("Resumen");
            else if (position == 1) tab.setText("Preguntas");
            else tab.setText("Análisis");
        }).attach();

        // Tab inicial: lee "initial_tab" si te lo mandan (por defecto 0 = Resumen)
        int initialTab = getArguments() != null ? getArguments().getInt("initial_tab", 0) : 0;
        pager.setCurrentItem(Math.max(0, Math.min(2, initialTab)), false);

        // id de sesión (OBLIGATORIO)
        int idSesion = getArguments() != null ? getArguments().getInt("id_sesion", 0) : 0;
        cargarDatos(idSesion);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Aplicar recarga por detalle ANTES de configurar padding
        // Esto asegura que la recarga se aplique cuando el usuario realmente ve el detalle
        aplicarRecargaPorDetalle();

        // Configurar padding del contenedor para bajar el contenido
        if (getActivity() != null) {
            android.widget.FrameLayout fragmentContainer = getActivity().findViewById(R.id.fragmentContainer);
            if (fragmentContainer != null) {
                // Usar post para asegurar que se aplique después de que la vista esté completamente montada
                fragmentContainer.post(() -> {
                    int paddingTop = (int) (40 * getResources().getDisplayMetrics().density); // 40dp
                    fragmentContainer.setPadding(0, paddingTop, 0, 0);
                });
                // También aplicar inmediatamente
                int paddingTop = (int) (40 * getResources().getDisplayMetrics().density); // 40dp
                fragmentContainer.setPadding(0, paddingTop, 0, 0);
            }
        }
    }

    /**
     * Aplica recarga de media vida por ver el detalle.
     * Solo funciona una vez por intento fallido.
     */
    private void aplicarRecargaPorDetalle() {
        try {
            // Obtener datos de los argumentos
            Bundle args = getArguments();
            if (args == null) {
                Log.w("FragmentDetalleSimulacro", "No se encontraron argumentos para aplicar recarga");
                return;
            }

            // Obtener materia y nivel
            String materia = args.getString("materia");
            int nivel = args.getInt("nivel", 0);

            if (materia == null || materia.isEmpty() || nivel <= 1) {
                Log.d("FragmentDetalleSimulacro", "No se aplica recarga: materia=" + materia + ", nivel=" + nivel);
                return;
            }

            // Obtener userId
            int userId = com.example.zavira_movil.local.TokenManager.getUserId(requireContext());
            if (userId <= 0) {
                Log.w("FragmentDetalleSimulacro", "No se pudo obtener userId");
                return;
            }

            // Aplicar recarga
            boolean recargado = com.example.zavira_movil.niveleshome.LivesManager.recargarPorDetalle(
                requireContext(),
                String.valueOf(userId),
                materia,
                nivel
            );

            if (recargado) {
                Log.d("FragmentDetalleSimulacro", "¡Media vida recargada al ver detalle!");
                Toast.makeText(requireContext(), "✨ ¡Media vida recargada!", Toast.LENGTH_SHORT).show();

                // Notificar a HomeActivity para actualizar UI de vidas
                if (getActivity() instanceof com.example.zavira_movil.Home.HomeActivity) {
                    // El HomeActivity debería tener un método para actualizar las vidas en el UI
                    // Por ahora, solo enviar un broadcast local
                    androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(requireContext())
                        .sendBroadcast(new android.content.Intent("UPDATE_LIVES"));
                }
            } else {
                Log.d("FragmentDetalleSimulacro", "No se pudo aplicar recarga (ya usada o no disponible)");
            }
        } catch (Exception e) {
            Log.e("FragmentDetalleSimulacro", "Error al aplicar recarga por detalle", e);
        }
    }

    private void cargarDatos(int idSesion) {
        if (!isAdded() || getContext() == null) return;
        
        if (idSesion <= 0) {
            Toast.makeText(getContext(), "Falta id del intento", Toast.LENGTH_LONG).show();
            return;
        }

        if (progress != null) progress.setVisibility(View.VISIBLE);

        // Usa tu RetrofitClient ya configurado (token, baseUrl, etc.)
        ProgresoService api = RetrofitClient
                .getInstance()
                .create(ProgresoService.class);

        Log.d("DETALLE_SIMU", "GET /movil/sesion/" + idSesion + "/detalle");

        api.getDetalleSesion(idSesion).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ProgresoDetalleResponse> call, @NonNull Response<ProgresoDetalleResponse> resp) {
                if (!isAdded() || getContext() == null) return;
                if (progress != null) progress.setVisibility(View.GONE);

                Log.d("DETALLE_SIMU", "========================================");
                Log.d("DETALLE_SIMU", "📥 RESPUESTA RECIBIDA DEL BACKEND");
                Log.d("DETALLE_SIMU", "========================================");
                Log.d("DETALLE_SIMU", "HTTP Status: " + resp.code());
                Log.d("DETALLE_SIMU", "Successful: " + resp.isSuccessful());

                if (!resp.isSuccessful() || resp.body() == null) {
                    Log.e("DETALLE_SIMU", "❌ ERROR: HTTP " + resp.code());
                    if (resp.body() == null) {
                        Log.e("DETALLE_SIMU", "❌ ERROR: Body es NULL");
                    }
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error " + resp.code(), Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                @NonNull ProgresoDetalleResponse d = resp.body();

                // Debugging detallado de la respuesta
                Log.d("DETALLE_SIMU", "========================================");
                Log.d("DETALLE_SIMU", "📊 DATOS RECIBIDOS:");
                Log.d("DETALLE_SIMU", "========================================");

                if (d.header == null) {
                    Log.e("DETALLE_SIMU", "❌ ERROR: Header es NULL");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Detalle sin header", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }

                // Logging del header
                Log.d("DETALLE_SIMU", "✅ HEADER:");
                Log.d("DETALLE_SIMU", "  - Materia: " + d.header.materia);
                Log.d("DETALLE_SIMU", "  - Fecha: " + d.header.fecha);
                Log.d("DETALLE_SIMU", "  - Nivel: " + d.header.nivel);
                Log.d("DETALLE_SIMU", "  - Correctas: " + d.header.correctas);
                Log.d("DETALLE_SIMU", "  - Incorrectas: " + d.header.incorrectas);
                Log.d("DETALLE_SIMU", "  - Total: " + d.header.total);
                Log.d("DETALLE_SIMU", "  - Puntaje: " + d.header.puntaje);
                Log.d("DETALLE_SIMU", "  - Tiempo Total (seg): " + d.header.tiempo_total_seg);
                Log.d("DETALLE_SIMU", "  - Escala: " + d.header.escala);

                // Verificar preguntas
                if (d.preguntas != null && !d.preguntas.isEmpty()) {
                    Log.d("DETALLE_SIMU", "✅ PREGUNTAS: " + d.preguntas.size() + " preguntas");
                    for (int i = 0; i < Math.min(3, d.preguntas.size()); i++) {
                        Log.d("DETALLE_SIMU", "  - Pregunta " + (i+1) + ": " +
                            (d.preguntas.get(i).enunciado != null ?
                                d.preguntas.get(i).enunciado.substring(0, Math.min(50, d.preguntas.get(i).enunciado.length())) + "..." :
                                "sin enunciado"));
                    }
                } else {
                    Log.w("DETALLE_SIMU", "⚠️ PREGUNTAS es NULL o vacío");
                    // Verificar si la sesión está vacía (usuario salió sin responder)
                    if (d.header.total == 0) {
                        Log.w("DETALLE_SIMU", "⚠️ SESIÓN VACÍA: El usuario salió sin responder ninguna pregunta");
                    }
                }

                // Logging de análisis
                if (d.analisis != null) {
                    Log.d("DETALLE_SIMU", "✅ ANÁLISIS: Datos disponibles");
                    // Nota: Los campos específicos dependen de la estructura de ProgresoDetalleResponse.Analisis
                } else {
                    Log.w("DETALLE_SIMU", "⚠️ ANÁLISIS es NULL");
                }

                Log.d("DETALLE_SIMU", "========================================");
                Log.d("DETALLE_SIMU", "🔄 BINDING DATOS A LA UI");
                Log.d("DETALLE_SIMU", "========================================");

                bindHeader(d);

                // Forzar color blanco en chips tras bind (defensa contra overrides posteriores)
                forceChipTextWhite();

                if (pagerAdapter != null) {
                    Log.d("DETALLE_SIMU", "✅ PagerAdapter existe, configurando datos...");
                    // Primero establecer la materia, luego los datos
                    pagerAdapter.setMateria(nullSafe(d.header.materia));
                    pagerAdapter.setData(d); // -> Resumen, Preguntas, Análisis
                    // Reforzar color blanco en chips en caso de que setData provoque algún cambio visual
                    forceChipTextWhite();
                    Log.d("DETALLE_SIMU", "✅ Datos configurados en PagerAdapter");
                } else {
                    Log.e("DETALLE_SIMU", "❌ ERROR: PagerAdapter es NULL");
                }

                Log.d("DETALLE_SIMU", "========================================");
            }

            @Override
            public void onFailure(@NonNull Call<ProgresoDetalleResponse> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) return;
                if (progress != null) progress.setVisibility(View.GONE);
                Log.e("DETALLE_SIMU", "onFailure", t);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Red: " + (t.getMessage() != null ? t.getMessage() : "Error desconocido"), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void bindHeader(ProgresoDetalleResponse d) {
        if (!isAdded() || getContext() == null) return;
        if (d == null || d.header == null) return;
        
        Log.d("DETALLE_SIMU", "🎨 BIND HEADER - Iniciando...");

        ProgresoDetalleResponse.Header h = d.header;
        android.content.Context ctx = getContext();

        // Texto principal
        if (tvMateria != null) {
            tvMateria.setText(nullSafe(h.materia));
            Log.d("DETALLE_SIMU", "✅ tvMateria actualizado: " + nullSafe(h.materia));
        } else {
            Log.e("DETALLE_SIMU", "❌ tvMateria es NULL");
        }

        if (tvFecha != null) {
            String fechaFormateada = formatFecha(h.fecha);
            tvFecha.setText(fechaFormateada);
            tvFecha.setTextColor(Color.BLACK);
            tvFecha.setTypeface(tvFecha.getTypeface(), android.graphics.Typeface.BOLD);
            Log.d("DETALLE_SIMU", "✅ tvFecha actualizado: " + fechaFormateada);
        } else {
            Log.e("DETALLE_SIMU", "❌ tvFecha es NULL");
        }

        if (tvNivel != null) {
            tvNivel.setText(nullSafe(h.nivel));
            Log.d("DETALLE_SIMU", "✅ tvNivel actualizado: " + nullSafe(h.nivel));
        } else {
            Log.e("DETALLE_SIMU", "❌ tvNivel es NULL");
        }

        if (tvTiempo != null) {
            String tiempoFormateado = toMin(h.tiempo_total_seg);
            tvTiempo.setText(tiempoFormateado);
            // Mostrar texto blanco para contraste sobre el chip azul
            tvTiempo.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            Log.d("DETALLE_SIMU", "✅ tvTiempo actualizado: " + tiempoFormateado + " (seg: " + h.tiempo_total_seg + ")");
        } else {
            Log.e("DETALLE_SIMU", "❌ tvTiempo es NULL");
        }

        if (tvCorr != null) {
            tvCorr.setText(String.valueOf(h.correctas));
            // Mostrar texto blanco para contraste sobre el chip verde
            tvCorr.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            Log.d("DETALLE_SIMU", "✅ tvCorr (correctas) actualizado: " + h.correctas);
        } else {
            Log.e("DETALLE_SIMU", "❌ tvCorr es NULL");
        }

        if (tvInc != null) {
            tvInc.setText(String.valueOf(h.incorrectas));
            // Mostrar texto blanco para contraste sobre el chip rojo
            tvInc.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            Log.d("DETALLE_SIMU", "✅ tvInc (incorrectas) actualizado: " + h.incorrectas);
        } else {
            Log.e("DETALLE_SIMU", "❌ tvInc es NULL");
        }

        // Porcentaje: si escala=porcentaje usa puntaje; si no, calcula según correctas/total
        int pct = (h.escala != null && h.escala.equalsIgnoreCase("porcentaje"))
                ? h.puntaje
                : (h.total > 0 ? Math.round(h.correctas * 100f / h.total) : 0);
        
        Log.d("DETALLE_SIMU", "📊 Cálculo porcentaje:");
        Log.d("DETALLE_SIMU", "  - Escala: " + h.escala);
        Log.d("DETALLE_SIMU", "  - Puntaje: " + h.puntaje);
        Log.d("DETALLE_SIMU", "  - Correctas: " + h.correctas + " / Total: " + h.total);
        Log.d("DETALLE_SIMU", "  - Porcentaje calculado: " + pct + "%");

        if (tvPuntaje != null) {
            tvPuntaje.setText(String.format(Locale.getDefault(), "%d%%", pct));
            // Obtener color del área para el porcentaje
            int areaColor = obtenerColorArea(nullSafe(h.materia), ctx);
            tvPuntaje.setTextColor(areaColor);
            Log.d("DETALLE_SIMU", "✅ tvPuntaje actualizado: " + pct + "%");
        } else {
            Log.e("DETALLE_SIMU", "❌ tvPuntaje es NULL");
        }

        if (progress != null) {
            progress.setMax(100);
            progress.setProgress(pct);
            Log.d("DETALLE_SIMU", "✅ ProgressBar actualizado: " + pct + "/100");
        } else {
            Log.e("DETALLE_SIMU", "❌ ProgressBar es NULL");
        }
        
        Log.d("DETALLE_SIMU", "🎨 BIND HEADER - Completado");
        // La materia ya se pasa al adapter en onResponse antes de setData
    }
    
    private int obtenerColorArea(String area, android.content.Context ctx) {
        if (area == null || ctx == null) return Color.parseColor("#B6B9C2");
        String a = area.toLowerCase().trim();
        
        try {
            // Isla del Conocimiento / Todas las áreas - Amarillo
            if (a.contains("conocimiento") || a.contains("isla") || 
                (a.contains("todas") && (a.contains("area") || a.contains("área")))) {
                return ContextCompat.getColor(ctx, R.color.area_conocimiento);
            }
            
            if (a.contains("matem")) return ContextCompat.getColor(ctx, R.color.area_matematicas);
            if (a.contains("lengua") || a.contains("lectura") || a.contains("espa") || a.contains("critica")) 
                return ContextCompat.getColor(ctx, R.color.area_lenguaje);
            if (a.contains("social") || a.contains("ciudad")) 
                return ContextCompat.getColor(ctx, R.color.area_sociales);
            if (a.contains("cien") || a.contains("biolo") || a.contains("fis") || a.contains("quim")) 
                return ContextCompat.getColor(ctx, R.color.area_ciencias);
            if (a.contains("ingl")) 
                return ContextCompat.getColor(ctx, R.color.area_ingles);
        } catch (Exception e) {
            return Color.parseColor("#B6B9C2");
        }
        
        return Color.parseColor("#B6B9C2");
    }

    // ---------- Helpers ----------
    private String toMin(int seg) {
        int m = seg / 60, s = seg % 60;
        return (m > 0) ? (m + " min" + (s > 0 ? " " + s + "s" : "")) : (s + "s");
    }

    private String formatFecha(String iso) {
        return (iso != null && iso.length() >= 10) ? iso.substring(0, 10) : "";
    }

    private String nullSafe(String s) { return s == null ? "" : s; }

    /**
     * Fuerza el color de texto blanco en los chips de Correctas/Incorrectas/Tiempo.
     * Método defensivo: llamado tras bindHeader y tras setData.
     */
    private void forceChipTextWhite() {
        if (!isAdded() || getContext() == null) return;
        android.content.Context ctx = getContext();
        try {
            if (tvCorr != null) tvCorr.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            if (tvInc != null) tvInc.setTextColor(ContextCompat.getColor(ctx, R.color.white));
            if (tvTiempo != null) tvTiempo.setTextColor(ContextCompat.getColor(ctx, R.color.white));
        } catch (Exception e) {
            Log.w("DETALLE_SIMU", "forceChipTextWhite error", e);
        }
    }
}
