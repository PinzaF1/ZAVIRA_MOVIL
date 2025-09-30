package com.example.zavira_movil;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.adapter.HistorialAdapter;
import com.example.zavira_movil.adapter.ProgresoAdapter;
import com.example.zavira_movil.model.HistorialItem;
import com.example.zavira_movil.model.ProgresoMateria;
import com.example.zavira_movil.model.ResumenGeneral;
import com.example.zavira_movil.remote.ApiService;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProgresoActivity extends AppCompatActivity {

    private CircularProgressIndicator progresoGeneral;
    private TextView textoProgreso;

    private TabLayout tabLayout;
    private FrameLayout contenedorTabs;

    private RecyclerView rvMaterias, rvHistorial;
    private ProgresoAdapter adapterMaterias;
    private HistorialAdapter adapterHistorial;

    private List<ProgresoMateria> listaMaterias = new ArrayList<>();
    private List<HistorialItem> listaHistorial = new ArrayList<>();

    private ApiService api;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progreso);

        // Referencias
        progresoGeneral = findViewById(R.id.progresoGeneral);
        textoProgreso = findViewById(R.id.textoProgreso);

        tabLayout = findViewById(R.id.tabLayout);
        contenedorTabs = findViewById(R.id.contenedorTabs);

        // Configurar TabLayout
        tabLayout.addTab(tabLayout.newTab().setText("General"));
        tabLayout.addTab(tabLayout.newTab().setText("Materias"));
        tabLayout.addTab(tabLayout.newTab().setText("Historial"));

        // Inicializar RecyclerViews
        rvMaterias = new RecyclerView(this);
        rvMaterias.setLayoutManager(new LinearLayoutManager(this));
        adapterMaterias = new ProgresoAdapter(listaMaterias);
        rvMaterias.setAdapter(adapterMaterias);

        rvHistorial = new RecyclerView(this);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        adapterHistorial = new HistorialAdapter(listaHistorial);
        rvHistorial.setAdapter(adapterHistorial);

        // Configurar Retrofit directo
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://tuapi.com/") // Cambia por tu URL base
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);

        // Pestañas
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                contenedorTabs.removeAllViews();

                switch (tab.getPosition()) {
                    case 0: // General
                        progresoGeneral.setVisibility(View.VISIBLE);
                        textoProgreso.setVisibility(View.VISIBLE);
                        break;

                    case 1: // Materias
                        progresoGeneral.setVisibility(View.GONE);
                        textoProgreso.setVisibility(View.GONE);
                        contenedorTabs.addView(rvMaterias);
                        break;

                    case 2: // Historial
                        progresoGeneral.setVisibility(View.GONE);
                        textoProgreso.setVisibility(View.GONE);
                        contenedorTabs.addView(rvHistorial);
                        break;
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Cargar datos
        cargarDatos();
    }

    private void cargarDatos() {
        // Progreso general
        api.getProgresoGeneral().enqueue(new Callback<ResumenGeneral>() {
            @Override
            public void onResponse(Call<ResumenGeneral> call, Response<ResumenGeneral> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int valor = response.body().getProgresoGeneral();
                    progresoGeneral.setProgress(valor);
                    textoProgreso.setText(valor + "%");
                }
            }

            @Override
            public void onFailure(Call<ResumenGeneral> call, Throwable t) {
                Toast.makeText(ProgresoActivity.this, "Error al cargar progreso general", Toast.LENGTH_SHORT).show();
            }
        });

        // Progreso por materias
        api.getProgresoMaterias().enqueue(new Callback<List<ProgresoMateria>>() {
            @Override
            public void onResponse(Call<List<ProgresoMateria>> call, Response<List<ProgresoMateria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaMaterias.clear();
                    listaMaterias.addAll(response.body());
                    adapterMaterias.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ProgresoMateria>> call, Throwable t) {
                Toast.makeText(ProgresoActivity.this, "Error al cargar materias", Toast.LENGTH_SHORT).show();
            }
        });

        // Historial
        api.getHistorial().enqueue(new Callback<List<HistorialItem>>() {
            @Override
            public void onResponse(Call<List<HistorialItem>> call, Response<List<HistorialItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaHistorial.clear();
                    listaHistorial.addAll(response.body());
                    adapterHistorial.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<HistorialItem>> call, Throwable t) {
                Toast.makeText(ProgresoActivity.this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
