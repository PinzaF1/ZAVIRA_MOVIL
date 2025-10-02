package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.adapter.HistorialAdapter;
import com.example.zavira_movil.model.HistorialItem;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHistorial extends Fragment {

    private RecyclerView recyclerHistorial;
    private HistorialAdapter historialAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        // Configuración RecyclerView
        recyclerHistorial = view.findViewById(R.id.recyclerHistorial);
        recyclerHistorial.setLayoutManager(new LinearLayoutManager(getContext()));
        historialAdapter = new HistorialAdapter(new ArrayList<>());
        recyclerHistorial.setAdapter(historialAdapter);

        // Cargar datos desde API
        cargarHistorial();

        return view;
    }

    private void cargarHistorial() {
        ApiService apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);
        apiService.getHistorial().enqueue(new Callback<List<HistorialItem>>() {
            @Override
            public void onResponse(Call<List<HistorialItem>> call, Response<List<HistorialItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<HistorialItem> historial = response.body();
                    historialAdapter.setLista(historial);
                    historialAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<HistorialItem>> call, Throwable t) {
                // Manejo de error
            }
        });
    }
}
