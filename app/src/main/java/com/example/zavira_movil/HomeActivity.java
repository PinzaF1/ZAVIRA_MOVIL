package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.local.TokenManager;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Si no hay token, ir al login
        if (TokenManager.getToken(this) == null) {
            irALoginYLimpiarPila();
            return;
        }

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // FAB → Perfil
        binding.fabPerfil.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });
    }

    private void irALoginYLimpiarPila() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
