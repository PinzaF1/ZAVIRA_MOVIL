package com.example.zavira_movil;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import com.example.zavira_movil.databinding.ActivityInfoTestBinding;

public class InfoTestActivity extends AppCompatActivity {

    private ActivityInfoTestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivityInfoTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.btnStartTest.setOnClickListener(v -> {

            Intent intent = new Intent(InfoTestActivity.this, TestAcademico.class);
            startActivity(intent);


            finish();
        });
    }
}
