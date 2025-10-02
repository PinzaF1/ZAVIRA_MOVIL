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
import com.example.zavira_movil.adapter.MateriasAdapter;
import com.example.zavira_movil.model.MateriaDetalle;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentMaterias extends Fragment {

    private RecyclerView recyclerMaterias;
    private MateriasAdapter materiaAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_materias, container, false);

        recyclerMaterias = view.findViewById(R.id.recyclerMaterias);
        recyclerMaterias.setLayoutManager(new LinearLayoutManager(getContext()));

        materiaAdapter = new MateriasAdapter(new ArrayList<>());
        recyclerMaterias.setAdapter(materiaAdapter);

        cargarMaterias();

        return view;
    }

    private void cargarMaterias() {
        ApiService apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);
        apiService.getMaterias().enqueue(new Callback<List<MateriaDetalle>>() {
            @Override
            public void onResponse(Call<List<MateriaDetalle>> call, Response<List<MateriaDetalle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MateriaDetalle> materias = response.body();
                    materiaAdapter.setLista(materias);
                    materiaAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<MateriaDetalle>> call, Throwable t) {}
        });
    }
}
