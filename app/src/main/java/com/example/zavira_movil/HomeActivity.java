package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.Home.SubjectAdapter;
import com.example.zavira_movil.model.DemoData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Top bar
        binding.btnBell.setOnClickListener(v ->
                Toast.makeText(this, "Notificaciones pronto", Toast.LENGTH_SHORT).show()
        );

        // FAB Perfil (si todavía es Activity)
        binding.fabPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class))
        );

        // Lista de materias
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubjects.setAdapter(new SubjectAdapter(DemoData.subjects()));

        // Bottom Navigation
        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setSelectedItemId(R.id.nav_islas);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_islas) {
                return true;
            } else if (id == R.id.nav_progreso) {
                startActivity(new Intent(this, ProgresoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_logros) {
                startActivity(new Intent(this, com.example.zavira_movil.ui.ranking.RankingLogrosActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }
}
