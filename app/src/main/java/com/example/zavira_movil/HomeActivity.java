package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.example.zavira_movil.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private ApiService api;

    private TextView tvNombre, tvDocumento, tvGrado, tvCurso, tvJornada, tvCorreo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tokenManager = new TokenManager(this);
        api = RetrofitClient.getInstance(this).create(ApiService.class);

        tvNombre = findViewById(R.id.tvNombre);
        tvDocumento = findViewById(R.id.tvDocumento);
        tvGrado = findViewById(R.id.tvGrado);
        tvCurso = findViewById(R.id.tvCurso);
        tvJornada = findViewById(R.id.tvJornada);
        tvCorreo = findViewById(R.id.tvCorreo);

        Button btnCerrar = findViewById(R.id.btnCerrarSesion);
        btnCerrar.setOnClickListener(v -> {
            tokenManager.clear();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Si no hay token, volver al login
        if (tokenManager.getToken() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        cargarPerfil();
    }

    private void cargarPerfil() {
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override
            public void onResponse(Call<Estudiante> call, Response<Estudiante> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(HomeActivity.this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                    return;
                }

                Estudiante e = response.body();
                tvNombre.setText("Nombre: " + safe(e.getNombreUsuario()) + " " + safe(e.getApellido()));
                tvDocumento.setText("Documento: " + safe(e.getNumeroDocumento()));
                tvGrado.setText("Grado: " + safe(e.getGrado()));
                tvCurso.setText("Curso: " + safe(e.getCurso()));
                tvJornada.setText("Jornada: " + safe(e.getJornada()));
                tvCorreo.setText("Correo: " + safe(e.getCorreo()));
            }

            @Override
            public void onFailure(Call<Estudiante> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }
}