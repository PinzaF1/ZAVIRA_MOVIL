package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.databinding.ActivityResultBinding;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding binding;   // View Binding
    private ApiService apiService;

    private String limpiarTexto(String s) {
        if (s == null) return "-";
        String out = s.replace("\\t", " ")
                .replace("\t", " ")
                .replace("\\n", "\n")
                .replace("\r", "");
        out = out.trim();
        return out.isEmpty() ? "-" : out;
    }

    private String formatearFechaFlexible(String fechaIso) {
        if (fechaIso == null || fechaIso.trim().isEmpty()) return "-";
        List<String> patrones = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd"
        );
        for (String p : patrones) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.getDefault());
                if (p.contains("'Z'") || p.endsWith("XXX")) {
                    in.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date d = in.parse(fechaIso);
                if (d != null) {
                    return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
                }
            } catch (ParseException ignored) {}
        }
        return fechaIso;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate con View Binding
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Navegación a HomeActivity
        binding.btnIrHome.setOnClickListener(v -> {
            Intent i = new Intent(ResultActivity.this, HomeActivity.class);
            // Limpia el back stack para no volver a esta pantalla
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        // Lee extras
        String extraNombre    = getIntent().getStringExtra("nombre");
        String extraFechaIso  = getIntent().getStringExtra("fecha");
        String extraEstiloDom = getIntent().getStringExtra("estilo");
        String extraCaract    = getIntent().getStringExtra("caracteristicas");
        String extraRecom     = getIntent().getStringExtra("recomendaciones");

        // Pinta UI
        binding.tvNombreCompleto.setText("Nombre completo: " +
                (extraNombre != null && !extraNombre.isEmpty() ? extraNombre : "-"));
        binding.tvFecha.setText("Fecha: " + formatearFechaFlexible(extraFechaIso));
        binding.tvEstilo.setText("Estilo: " +
                (extraEstiloDom != null && !extraEstiloDom.isEmpty() ? extraEstiloDom : "-"));
        binding.tvCaracteristicas.setText("Características: " + limpiarTexto(extraCaract));
        binding.tvRecomendaciones.setText("Recomendaciones: " + limpiarTexto(extraRecom));

        boolean faltaNombre = (extraNombre == null || extraNombre.trim().isEmpty());
        boolean faltaFecha  = (extraFechaIso == null || extraFechaIso.trim().isEmpty());
        boolean faltanTextos = (extraCaract == null || extraCaract.trim().isEmpty())
                || (extraRecom == null || extraRecom.trim().isEmpty());

        if (faltaNombre || faltaFecha || faltanTextos) {
            apiService = RetrofitClient.getInstance(this).create(ApiService.class);
            apiService.obtenerResultado().enqueue(new Callback<KolbResultado>() {
                @Override
                public void onResponse(Call<KolbResultado> call, Response<KolbResultado> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e("KOLB_RESULT", "GET /kolb/resultado code=" + response.code());
                        return;
                    }
                    KolbResultado r = response.body();

                    if (faltaNombre) {
                        String nombreCompleto = ((r.getNombre() != null) ? r.getNombre() : "")
                                + " " + ((r.getApellido() != null) ? r.getApellido() : "");
                        nombreCompleto = nombreCompleto.trim();
                        if (nombreCompleto.isEmpty()) nombreCompleto = "-";
                        binding.tvNombreCompleto.setText("Nombre completo: " + nombreCompleto);
                    }
                    if (faltaFecha) {
                        binding.tvFecha.setText("Fecha: " + formatearFechaFlexible(r.getFecha()));
                    }
                    if (extraCaract == null || extraCaract.trim().isEmpty()) {
                        binding.tvCaracteristicas.setText("Características: " + limpiarTexto(r.getCaracteristicas()));
                    }
                    if (extraRecom == null || extraRecom.trim().isEmpty()) {
                        binding.tvRecomendaciones.setText("Recomendaciones: " + limpiarTexto(r.getRecomendaciones()));
                    }
                    if (extraEstiloDom == null || extraEstiloDom.trim().isEmpty()) {
                        binding.tvEstilo.setText("Estilo: " + (r.getEstilo() != null ? r.getEstilo() : "-"));
                    }
                }

                @Override
                public void onFailure(Call<KolbResultado> call, Throwable t) {
                    Log.e("KOLB_RESULT", "GET /kolb/resultado fail", t);
                    Toast.makeText(ResultActivity.this, "No se pudo completar el resultado", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
