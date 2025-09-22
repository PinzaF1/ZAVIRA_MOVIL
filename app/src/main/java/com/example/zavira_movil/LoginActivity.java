package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.model.LoginResponse;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etDocumento, etPassword;
    private ProgressBar progress;
    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etDocumento = findViewById(R.id.etDocumento);
        etPassword  = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);

        api = RetrofitClient.getInstance(this).create(ApiService.class);

        if (TokenManager.getToken(this) != null) {
            goToHome();
            return;
        }

        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doLogin() {
        String doc = etDocumento.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (doc.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Documento y contraseña son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);

        LoginRequest request = new LoginRequest(doc, pass);
        api.loginEstudiante(request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progress.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this, "Error en el login", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    String body = response.body().string().trim();
                    LoginResponse loginResponse = new Gson().fromJson(body, LoginResponse.class);

                    if (loginResponse.getToken() == null || loginResponse.getToken().isEmpty()) {
                        Toast.makeText(LoginActivity.this, "No se recibió token", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Guardar token
                    TokenManager.setToken(LoginActivity.this, loginResponse.getToken());
                    Log.d("TOKEN_GUARDADO", loginResponse.getToken());

                    // Guardar userId extraído del JWT
                    int userId = TokenManager.extractUserIdFromJwt(loginResponse.getToken());
                    if (userId > 0) {
                        TokenManager.setUserId(LoginActivity.this, userId);
                        Log.d("USER_ID_GUARDADO", "id=" + userId);
                    } else {
                        Log.w("USER_ID_GUARDADO", "No se pudo extraer el id del JWT");
                    }

                    Toast.makeText(LoginActivity.this, "Bienvenido/a", Toast.LENGTH_SHORT).show();
                    goToHome();

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Credenciales Incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progress.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Fallo de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void goToHome() {
        Intent i = new Intent(this, InfoTestActivity.class);
        startActivity(i);
        finish();
    }
}