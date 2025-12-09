package com.example.zavira_movil.detalleprogreso;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class FragmentDetalleResumen extends Fragment {
    private TextView tvMensaje, tvNivelActual, tvNivelActualValue;
    private android.view.View llNivelActual;
    private RecyclerView rvStats;
    private ProgresoDetalleResponse data;
    private String materia;

    // Nuevas vistas
    private CircularProgressIndicator progresoResumen;
    private TextView tvPctCenter, tvBadgeCorrectas, tvBadgeIncorrectas, tvBadgeTiempo;
    private android.view.View llRecomendaciones;
    private android.view.View containerRecomendaciones;

    public void setData(ProgresoDetalleResponse data) {
        this.data = data;
        if (getView() != null && isAdded()) bind();
    }
    
    public void setMateria(String materia) {
        this.materia = materia;
        if (getView() != null && isAdded()) bind();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inf, @Nullable ViewGroup c, @Nullable Bundle s) {
        View v = inf.inflate(R.layout.fragment_detalle_resumen, c, false);
        tvMensaje = v.findViewById(R.id.tvMensaje);
        tvNivelActual = v.findViewById(R.id.tvNivelActual);
        tvNivelActualValue = v.findViewById(R.id.tvNivelActualValue);
        llNivelActual = v.findViewById(R.id.llNivelActual);
        rvStats = v.findViewById(R.id.rvStats);

        // nuevas vistas
        progresoResumen = v.findViewById(R.id.progresoResumen);
        tvPctCenter = v.findViewById(R.id.tvPctCenter);
        tvBadgeCorrectas = v.findViewById(R.id.tvBadgeCorrectas);
        tvBadgeIncorrectas = v.findViewById(R.id.tvBadgeIncorrectas);
        tvBadgeTiempo = v.findViewById(R.id.tvBadgeTiempo);
        llRecomendaciones = v.findViewById(R.id.llRecomendaciones);
        containerRecomendaciones = v.findViewById(R.id.containerRecomendaciones);

        if (getContext() != null) {
            rvStats.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        if (data != null && isAdded()) bind();
        return v;
    }

    private void bind() {
        if (!isAdded() || getContext() == null) return;
        if (data == null || data.resumen == null || data.header == null) return;
        if (tvMensaje == null) return;
        
        // Mensaje centrado con signos de admiración y negrita
        String mensaje = data.resumen.mensaje != null ? data.resumen.mensaje.trim() : "";
        if (!mensaje.isEmpty()) {
            // Agregar signo de apertura si no existe
            if (!mensaje.startsWith("¡")) {
                mensaje = "¡" + mensaje;
            }
            // Agregar signo de cierre si no existe
            if (!mensaje.endsWith("!") && !mensaje.endsWith("¡")) {
                mensaje = mensaje + "!";
            }
        }
        tvMensaje.setText(mensaje);
        tvMensaje.setGravity(Gravity.CENTER);
        tvMensaje.setTypeface(null, Typeface.BOLD);
        
        // Nivel actual: mostrar u ocultar
        if (materia != null) {
            String m = materia.toLowerCase().trim();
            boolean esTodasLasAreas = m.contains("conocimiento") || m.contains("isla") || 
                (m.contains("todas") && (m.contains("area") || m.contains("área")));
            if (esTodasLasAreas) {
                if (llNivelActual != null) llNivelActual.setVisibility(View.GONE);
            } else {
                if (llNivelActual != null) llNivelActual.setVisibility(View.VISIBLE);
                if (tvNivelActual != null) {
                    tvNivelActual.setText("Nivel actual:");
                    tvNivelActual.setTypeface(null, Typeface.BOLD);
                }
                if (tvNivelActualValue != null) {
                    tvNivelActualValue.setText(String.valueOf(data.resumen.nivelActual));
                    tvNivelActualValue.setTypeface(null, Typeface.BOLD);
                }
            }
        } else {
            if (llNivelActual != null) llNivelActual.setVisibility(View.VISIBLE);
            if (tvNivelActual != null) { tvNivelActual.setText("Nivel actual:"); tvNivelActual.setTypeface(null, Typeface.BOLD); }
            if (tvNivelActualValue != null) { tvNivelActualValue.setText(String.valueOf(data.resumen.nivelActual)); tvNivelActualValue.setTypeface(null, Typeface.BOLD); }
        }

        // Estadísticas: badges y progress
        int total = data.header.total;
        int correctas = data.header.correctas;
        int incorrectas = data.header.incorrectas;
        int tiempo = data.header.tiempo_total_seg;

        if (progresoResumen != null) {
            int pct = (data.header.escala != null && data.header.escala.equalsIgnoreCase("porcentaje")) ? data.header.puntaje
                    : (total > 0 ? Math.round(correctas * 100f / total) : 0);
            progresoResumen.setMax(100);
            progresoResumen.setProgress(pct);
            if (tvPctCenter != null) tvPctCenter.setText(String.format("%d%%", pct));
        }

        if (tvBadgeCorrectas != null) tvBadgeCorrectas.setText(String.format("%d\nCorrectas", correctas));
        if (tvBadgeIncorrectas != null) tvBadgeIncorrectas.setText(String.format("%d\nIncorrectas", incorrectas));
        if (tvBadgeTiempo != null) tvBadgeTiempo.setText(String.format("%ds\nTiempo", tiempo));

        // RecyclerView de estadísticas detalladas
        if (rvStats != null) {
            rvStats.setAdapter(new ResumenAdapter(total, correctas, incorrectas, tiempo));
        }

        // Recomendaciones: mostrar si hay datos en data.analisis.recomendaciones (si existe)
        if (llRecomendaciones != null && containerRecomendaciones != null) {
            containerRecomendaciones.setVisibility(View.GONE);
            if (data.analisis != null) {
                List<String> recs = null;
                try { recs = data.analisis.recomendaciones; } catch (Exception e) { recs = null; }
                if (recs != null && !recs.isEmpty()) {
                    containerRecomendaciones.setVisibility(View.VISIBLE);
                    containerRecomendaciones.setFocusable(false);
                    containerRecomendaciones.clearFocus();
                    containerRecomendaciones.setVisibility(View.VISIBLE);
                    containerRecomendaciones.setEnabled(true);
                    // limpiar
                    if (containerRecomendaciones instanceof android.view.ViewGroup) {
                        ((android.view.ViewGroup) containerRecomendaciones).removeAllViews();
                        for (String r : recs) {
                            TextView t = new TextView(getContext());
                            t.setText("• " + r);
                            t.setPadding(6,6,6,6);
                            ((android.view.ViewGroup) containerRecomendaciones).addView(t);
                        }
                        llRecomendaciones.setVisibility(View.VISIBLE);
                    }
                } else {
                    llRecomendaciones.setVisibility(View.GONE);
                }
            } else {
                llRecomendaciones.setVisibility(View.GONE);
            }
        }
    }
}
