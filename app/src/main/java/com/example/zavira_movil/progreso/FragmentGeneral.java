package com.example.zavira_movil.progreso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.R;
import com.example.zavira_movil.model.ResumenGeneral;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.github.lzyzsd.circleprogress.DonutProgress;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentGeneral extends Fragment {

    private DonutProgress progresoCircular;
    private TextView tvNivelActual, tvNombreNivel;
    private ProgressBar pbBasico, pbIntermedio, pbAvanzado, pbExperto;
    private TextView tvPorcBasico, tvPorcIntermedio, tvPorcAvanzado, tvPorcExperto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_general, container, false);

        // Donut y nivel global
        progresoCircular = view.findViewById(R.id.progresoCircular);
        tvNivelActual = view.findViewById(R.id.tvNivelActual);
        tvNombreNivel = view.findViewById(R.id.tvNombreNivel);

        // Barras de niveles
        pbBasico = view.findViewById(R.id.pbBasico);
        pbIntermedio = view.findViewById(R.id.pbIntermedio);
        pbAvanzado = view.findViewById(R.id.pbAvanzado);
        pbExperto = view.findViewById(R.id.pbExperto);

        // TextViews de porcentaje encima de barras
        tvPorcBasico = view.findViewById(R.id.tvPorcBasico);
        tvPorcIntermedio = view.findViewById(R.id.tvPorcIntermedio);
        tvPorcAvanzado = view.findViewById(R.id.tvPorcAvanzado);
        tvPorcExperto = view.findViewById(R.id.tvPorcExperto);

        cargarResumen();

        return view;
    }

    private void cargarResumen() {
        ApiService apiService = RetrofitClient.getInstance(getContext()).create(ApiService.class);
        apiService.getProgresoGeneral().enqueue(new Callback<ResumenGeneral>() {
            @Override
            public void onResponse(Call<ResumenGeneral> call, Response<ResumenGeneral> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResumenGeneral resumen = response.body();

                    // Progreso global
                    progresoCircular.setProgress(resumen.getPorcentajeCompletado());
                    progresoCircular.setText(resumen.getPorcentajeCompletado() + "%");

                    // Nivel actual con porcentaje
                    tvNivelActual.setText("Nivel actual: " + resumen.getPorcentajeCompletado() + "%");
                    tvNombreNivel.setText(resumen.getNivelActual());

                    // Llenar barras de nivel con porcentaje
                    pbBasico.setProgress(Math.round(resumen.getProgresoBasico()));
                    tvPorcBasico.setText(Math.round(resumen.getProgresoBasico()) + "%");

                    pbIntermedio.setProgress(Math.round(resumen.getProgresoIntermedio()));
                    tvPorcIntermedio.setText(Math.round(resumen.getProgresoIntermedio()) + "%");

                    pbAvanzado.setProgress(Math.round(resumen.getProgresoAvanzado()));
                    tvPorcAvanzado.setText(Math.round(resumen.getProgresoAvanzado()) + "%");

                    pbExperto.setProgress(Math.round(resumen.getProgresoExperto()));
                    tvPorcExperto.setText(Math.round(resumen.getProgresoExperto()) + "%");

                } else {
                    Log.e("API", "Error en respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResumenGeneral> call, Throwable t) {
                Log.e("API", "Error en petición", t);
            }
        });
    }
}
