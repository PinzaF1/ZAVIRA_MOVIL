package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.zavira_movil.databinding.ActivityHomeBinding;
import com.example.zavira_movil.Home.SubjectAdapter;
import com.example.zavira_movil.model.DemoData;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Acción campana
        binding.btnBell.setOnClickListener(v ->
                Toast.makeText(this, "Notificaciones pronto ", Toast.LENGTH_SHORT).show());

        // Acción FAB perfil
        binding.fabPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        // Lista de materias
        binding.rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSubjects.setAdapter(new SubjectAdapter(DemoData.subjects()));
    }
}
