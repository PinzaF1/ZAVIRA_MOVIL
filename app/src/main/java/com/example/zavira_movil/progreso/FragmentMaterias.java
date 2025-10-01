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
import com.example.zavira_movil.adapter.ProgresoAdapter;
import com.example.zavira_movil.model.MateriaProgreso;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMaterias extends Fragment {

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materias, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewMaterias);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Aquí va tu código para consumir la API
        ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
        Call<List<MateriaProgreso>> call = api.getProgresoMaterias();
        call.enqueue(new Callback<List<MateriaProgreso>>() {
            @Override
            public void onResponse(Call<List<MateriaProgreso>> call, Response<List<MateriaProgreso>> response) {
                if(response.isSuccessful() && response.body() != null){
                    List<MateriaProgreso> materias = response.body();

                    // Pasamos la lista al Adapter
                    ProgresoAdapter adapter = new ProgresoAdapter(materias);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<MateriaProgreso>> call, Throwable t) {
                t.printStackTrace(); // Si hay error lo ves en Logcat
            }
        });
    }
}
