package com.example.zavira_movil;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.KolbResultado;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

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

        // Instancia de API
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);

        // Recuperar token guardado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Token NO encontrado en SharedPreferences", Toast.LENGTH_LONG).show();
            Log.d("RESULT_ACTIVITY_TOKEN", "Token leído de SharedPreferences: " + token);
            return;
        }

        Log.d("TOKEN_ENVIADO", "Bearer " + token);

        Toast.makeText(this, " Token recuperado correctamente", Toast.LENGTH_SHORT).show();


        // Llamada al backend
        Call<KolbResultado> call = apiService.obtenerResultado("Bearer " + token);

        call.enqueue(new Callback<KolbResultado>() {
            @Override
            public void onResponse(Call<KolbResultado> call, Response<KolbResultado> response) {
                Log.d("KOLB_RESPONSE_CODE", String.valueOf(response.code()));
                if (response.isSuccessful() && response.body() != null) {
                    KolbResultado resultado = response.body();

                    Log.d("KOLB_RESULT_RAW", resultado.toString()); // Implementa toString() si quieres

                    // Mostrar datos en la UI
                    String nombreCompleto = resultado.getNombre() + " " + resultado.getApellido();
                    tvNombreCompleto.setText("Nombre completo: " + nombreCompleto);
                    tvFecha.setText("Fecha: " + resultado.getFecha());
                    tvEstilo.setText("Estilo: " + resultado.getEstilo());
                    tvCaracteristicas.setText("Características: " + resultado.getCaracteristicas());
                    tvRecomendaciones.setText("Recomendaciones: " + resultado.getRecomendaciones());
                } else {
                    try {
                        Log.e("KOLB_ERROR_BODY", response.errorBody().string());
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
