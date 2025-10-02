package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.ResumenGeneral;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentGeneral extends Fragment {

    private CircularProgressIndicator progresoGeneral;
    private TextView textoProgreso;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_general, container, false);

        progresoGeneral = view.findViewById(R.id.progresoGeneral);
        textoProgreso = view.findViewById(R.id.textoProgreso);

        cargarResumen();

        return view;
    }

    private void cargarResumen() {
        ApiService apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);
        apiService.getResumen().enqueue(new Callback<ResumenGeneral>() {
            @Override
            public void onResponse(Call<ResumenGeneral> call, Response<ResumenGeneral> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResumenGeneral resumen = response.body();

                    Log.d("API", "Progreso recibido: " + resumen.getValor());

                    progresoGeneral.setProgress(resumen.getValor());
                    textoProgreso.setText(resumen.getValor() + "%");
                } else {
                    Log.e("API", "Error en respuesta: " + response.code());
                    textoProgreso.setText("Error al cargar");
                }
            }

            @Override
            public void onFailure(Call<ResumenGeneral> call, Throwable t) {
                Log.e("API", "Fallo de conexión: " + t.getMessage());
                textoProgreso.setText("Error al cargar");
            }
        });
    }
}
