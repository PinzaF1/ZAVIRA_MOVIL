package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private ApiService api;

    private TextView tvNombre, tvDocumento, tvGrado, tvCurso, tvJornada, tvCorreo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Si no hay token, ir al login
        if (TokenManager.getToken(this) == null) {
            irALoginYLimpiarPila();
            return;
        }

        api = RetrofitClient.getInstance(this).create(ApiService.class);

        tvNombre    = findViewById(R.id.tvNombre);
        tvDocumento = findViewById(R.id.tvDocumento);
        tvGrado     = findViewById(R.id.tvGrado);
        tvCurso     = findViewById(R.id.tvCurso);
        tvJornada   = findViewById(R.id.tvJornada);
        tvCorreo    = findViewById(R.id.tvCorreo);

        Button btnCerrar = findViewById(R.id.btnCerrarSesion);
        btnCerrar.setOnClickListener(v -> cerrarSesion());

        cargarPerfil();
    }

    private void cargarPerfil() {
        api.getPerfilEstudiante().enqueue(new Callback<Estudiante>() {
            @Override
            public void onResponse(Call<Estudiante> call, Response<Estudiante> response) {
                if (!response.isSuccessful()) {
                    // Loguea el error del servidor para depurar
                    try {
                        ResponseBody err = response.errorBody();
                        Log.e("HOME_PERFIL", "HTTP " + response.code() + " - " + (err != null ? err.string() : "sin cuerpo"));
                    } catch (Exception ignored) {}
                    Toast.makeText(HomeActivity.this, "No se pudo cargar el perfil", Toast.LENGTH_SHORT).show();
                    return;
                }

                Estudiante e = response.body();
                if (e == null) {
                    Toast.makeText(HomeActivity.this, "Perfil vacío", Toast.LENGTH_SHORT).show();
                    return;
                }

                tvNombre.setText("Nombre: "     + safe(e.getNombreUsuario())    + " " + safe(e.getApellido()));
                tvDocumento.setText("Documento: " + safe(e.getNumeroDocumento()));
                tvGrado.setText("Grado: "       + safe(e.getGrado()));
                tvCurso.setText("Curso: "       + safe(e.getCurso()));
                tvJornada.setText("Jornada: "   + safe(e.getJornada()));
                tvCorreo.setText("Correo: "     + safe(e.getCorreo()));
            }

            @Override
            public void onFailure(Call<Estudiante> call, Throwable t) {
                Log.e("HOME_PERFIL_FAIL", "onFailure", t);
                Toast.makeText(HomeActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cerrarSesion() {
        // Limpia token e id de usuario
        TokenManager.clearAll(this);
        // Redirige al Login y limpia el back stack
        irALoginYLimpiarPila();
    }

    private void irALoginYLimpiarPila() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}