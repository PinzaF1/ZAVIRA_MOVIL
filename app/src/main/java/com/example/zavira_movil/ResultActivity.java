package com.example.zavira_movil;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.example.zavira_movil.model.KolbResultado;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private TextView tvNombreCompleto, tvFecha, tvEstilo, tvCaracteristicas, tvRecomendaciones;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Referencias a los TextView
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto);
        tvFecha = findViewById(R.id.tvFecha);
        tvEstilo = findViewById(R.id.tvEstilo);
        tvCaracteristicas = findViewById(R.id.tvCaracteristicas);
        tvRecomendaciones = findViewById(R.id.tvRecomendaciones);

        // Instancia de API con interceptor
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        obtenerResultadoKolb();
    }

    private void obtenerResultadoKolb() {
        Call<KolbResultado> call = apiService.obtenerResultado();

        call.enqueue(new Callback<KolbResultado>() {
            @Override
            public void onResponse(Call<KolbResultado> call, Response<KolbResultado> response) {
                Log.d("KOLB_RESPONSE_CODE", String.valueOf(response.code()));

                if (response.isSuccessful() && response.body() != null) {
                    KolbResultado resultado = response.body();

                    // Mostrar datos en la UI
                    String nombreCompleto = resultado.getNombre() + " " + resultado.getApellido();
                    tvNombreCompleto.setText("Nombre completo: " + nombreCompleto);
                    tvFecha.setText("Fecha: " + resultado.getFecha());
                    tvEstilo.setText("Estilo: " + resultado.getEstilo());
                    tvCaracteristicas.setText("Características: " + resultado.getCaracteristicas());
                    tvRecomendaciones.setText("Recomendaciones: " + resultado.getRecomendaciones());

                    Log.d("KOLB_RESULT", "Resultado recibido correctamente");

                } else {
                    // Mostrar error detallado
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "N/A";
                        Log.e("KOLB_ERROR_BODY", errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("KOLB_ERROR", "Error en respuesta: " + response.code());
                    Toast.makeText(ResultActivity.this, "Error al obtener resultado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<KolbResultado> call, Throwable t) {
                Toast.makeText(ResultActivity.this, "Fallo en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("KOLB_FAIL", "onFailure: ", t);
            }
        });
    }
}
