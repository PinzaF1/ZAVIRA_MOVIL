package com.example.zavira_movil.progreso;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.detalleprogreso.FragmentDetalleSimulacro;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentHistorial extends Fragment {

    private static final String TAG = "FragmentHistorial";
    public static final String ACTION_HISTORIAL_UPDATE = "com.example.zavira_movil.HISTORIAL_UPDATE";

    private RecyclerView rv;
    private ProgressBar progress;
    private TextView tvError;
    private HistorialAdapter adapter;

    private int currentPage = 1;
    private final int pageSize = 20;

    // BroadcastReceiver para actualizaciones en tiempo real
    private BroadcastReceiver historialReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_HISTORIAL_UPDATE.equals(intent.getAction())) {
                Log.d(TAG, "🔄 Broadcast recibido - Actualizando historial en tiempo real");
                recargarHistorial();
            }
        }
    };

    public FragmentHistorial() { super(R.layout.fragment_historial); }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rv = v.findViewById(R.id.rvPreguntas);
        progress = v.findViewById(R.id.progress);
        tvError = v.findViewById(R.id.tvError);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistorialAdapter();
        rv.setAdapter(adapter);

        // Click en item -> abrir detalle (Resumen)
        adapter.setOnItemClick(this::abrirDetalle);

        // Registrar BroadcastReceiver para actualizaciones en tiempo real
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(historialReceiver, new IntentFilter(ACTION_HISTORIAL_UPDATE));
        Log.d(TAG, "✅ BroadcastReceiver registrado para actualizaciones en tiempo real");

        cargarHistorial(currentPage, pageSize);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar historial cada vez que el usuario vuelva a este fragmento
        Log.d(TAG, "📱 onResume - Recargando historial automáticamente");
        recargarHistorial();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Desregistrar BroadcastReceiver
        try {
            LocalBroadcastManager.getInstance(requireContext())
                    .unregisterReceiver(historialReceiver);
            Log.d(TAG, "✅ BroadcastReceiver desregistrado");
        } catch (Exception e) {
            Log.e(TAG, "Error al desregistrar receiver", e);
        }
        rv = null; progress = null; tvError = null;
    }

    // Método para recargar el historial sin mostrar loading excesivo
    private void recargarHistorial() {
        if (!isAdded() || getContext() == null) return;

        Log.d(TAG, "🔄 Recargando historial...");
        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getHistorial(currentPage, pageSize).enqueue(new Callback<HistorialResponse>() {
            @Override
            public void onResponse(Call<HistorialResponse> call, Response<HistorialResponse> resp) {
                if (!isAdded()) return;

                if (!resp.isSuccessful() || resp.body() == null) {
                    Log.e(TAG, "❌ Error al recargar historial: HTTP " + resp.code());
                    return;
                }

                List<HistorialItem> items = resp.body().getItems();
                if (items != null && !items.isEmpty()) {
                    Log.d(TAG, "✅ Historial actualizado: " + items.size() + " items");
                    tvError.setVisibility(View.GONE);
                    adapter.setData(items);
                } else {
                    Log.w(TAG, "⚠️ Historial vacío");
                }
            }

            @Override
            public void onFailure(Call<HistorialResponse> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "❌ Error de red al recargar historial: " + t.getMessage());
            }
        });
    }

    private void cargarHistorial(int page, int limit) {
        mostrarCargando(true);
        tvError.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getHistorial(page, limit).enqueue(new Callback<HistorialResponse>() {
            @Override
            public void onResponse(Call<HistorialResponse> call, Response<HistorialResponse> resp) {
                mostrarCargando(false);

                if (!resp.isSuccessful() || resp.body() == null) {
                    mostrarError("HTTP " + resp.code());
                    return;
                }

                List<HistorialItem> items = resp.body().getItems();
                if (items == null || items.isEmpty()) {
                    mostrarError("Sin historial disponible");
                } else {
                    tvError.setVisibility(View.GONE);
                    adapter.setData(items);
                }
            }

            @Override
            public void onFailure(Call<HistorialResponse> call, Throwable t) {
                mostrarCargando(false);
                mostrarError("Fallo de red: " + (t.getMessage() != null ? t.getMessage() : ""));
            }
        });
    }

    private void mostrarCargando(boolean s) {
        if (progress != null) progress.setVisibility(s ? View.VISIBLE : View.GONE);
    }

    private void mostrarError(String msg) {
        if (tvError != null) {
            tvError.setText(msg != null ? msg : "Error");
            tvError.setVisibility(View.VISIBLE);
        }
        if (adapter != null) adapter.setData(Collections.emptyList());
    }

    // -------- Navegación al detalle (ENVÍA id_sesion) --------
    private void abrirDetalle(HistorialItem it) {
        if (!isAdded()) return;

        // Validar disponibilidad de detalle
        if (it != null && !it.isDetalleDisponible()) {
            android.widget.Toast.makeText(requireContext(),
                    "Este intento no tiene detalle disponible",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Parsear intentoId -> id_sesion (int)
        int idSesion = safeParseInt(it != null ? it.getIntentoId() : null);
        if (idSesion <= 0) {
            android.widget.Toast.makeText(requireContext(),
                    "id_sesion faltante", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Argumentos para el detalle
        Bundle b = new Bundle();
        b.putInt("id_sesion", idSesion);

        // Opcionales: puedes mostrarlos de inmediato mientras carga
        if (it != null) {
            b.putString("materia", it.getMateria());
            b.putInt("porcentaje", it.getPorcentaje());
            b.putString("nivel", it.getNivel());
            b.putString("fecha", it.getFecha());
        }

        //  Forzar que el detalle abra en la pestaña "Resumen" (posición 0)
        b.putInt("initial_tab", 0); // el FragmentDetalleSimulacro puede leerlo y hacer pager.setCurrentItem(0,false)

        Fragment f = new FragmentDetalleSimulacro();
        f.setArguments(b);

        int root = idByName("fragmentContainer");
        if (root == 0) root = idByName("main_container");
        if (root == 0) root = android.R.id.content;

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(root, f)
                .addToBackStack("detalleSimulacro")
                .commit();
    }

    // helper para resolver id por nombre sin acoplarte a una sola Activity
    private int idByName(String name) {
        try {
            return getResources().getIdentifier(name, "id", requireContext().getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    // parseo seguro de String -> int
    private int safeParseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
}
