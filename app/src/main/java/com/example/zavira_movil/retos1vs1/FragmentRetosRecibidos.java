package com.example.zavira_movil.retos1vs1;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.R;
import com.example.zavira_movil.local.TokenManager;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRetosRecibidos extends Fragment {

    private ProgressBar pb;
    private TextView tvEmpty;
    private RecyclerView rv;
    private RecibidosAdapter adapter;

    // QUITAR EL POLLING AUTOMÁTICO - ya no se usa
    // private Handler handler;
    // private int polls = 0;
    // private final Runnable poller = this::cargar;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inf, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inf.inflate(R.layout.fragment_retos_recibidos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        super.onViewCreated(v, s);

        pb      = v.findViewById(R.id.progressRecibidos);
        tvEmpty = v.findViewById(R.id.tvEmpty);
        rv      = v.findViewById(R.id.rvRecibidos);

        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecibidosAdapter(this::aceptarYIrSala);
        adapter.setOnRechazarClick(this::rechazarReto);
        rv.setAdapter(adapter);

        // QUITAR EL POLLING AUTOMÁTICO - solo cargar una vez
        // handler = new Handler();
        // polls = 0;
        cargar();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refrescar cuando se vuelve a la pantalla (para ver nuevos retos recibidos)
        cargar();
    }

    @Override
    public void onDestroyView() {
        // QUITAR EL POLLING AUTOMÁTICO - ya no se usa
        // if (handler != null) { handler.removeCallbacksAndMessages(null); handler = null; }
        super.onDestroyView();
    }

    private void cargar() {
        if (!isAdded()) return;
        mostrarCargando(true);
        tvEmpty.setVisibility(View.GONE);

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        Call<List<RetoListItem>> call = api.listarRetos("recibidos");

        call.enqueue(new Callback<List<RetoListItem>>() {
            @Override public void onResponse(Call<List<RetoListItem>> call, Response<List<RetoListItem>> resp) {
                if (!isAdded()) return;
                mostrarCargando(false);

                List<RetoListItem> lista = (resp.isSuccessful() && resp.body() != null)
                        ? resp.body()
                        : new ArrayList<>();

                adapter.setData(lista);

                if (lista.isEmpty()) {
                    tvEmpty.setText("No tienes retos recibidos.");
                    tvEmpty.setVisibility(View.VISIBLE);
                    // QUITAR EL POLLING AUTOMÁTICO - solo mostrar mensaje
                    // No hacer polling, el usuario debe refrescar manualmente o usar onResume
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            }

            @Override public void onFailure(Call<List<RetoListItem>> call, Throwable t) {
                if (!isAdded()) return;
                mostrarCargando(false);
                tvEmpty.setText("No tienes retos recibidos.");
                tvEmpty.setVisibility(View.VISIBLE);
                adapter.setData(new ArrayList<>());
                // QUITAR EL POLLING AUTOMÁTICO - solo mostrar mensaje
                // No hacer polling, el usuario debe refrescar manualmente o usar onResume
            }
        });
    }

    /** Acepta el reto y abre sala de espera. El quiz comenzará cuando el estado sea 'en_curso'. */
    private void aceptarYIrSala(RetoListItem it) {
        if (!isAdded()) return;

        if (it == null || it.getIdReto() == null) {
            Toast.makeText(requireContext(), "Reto inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        mostrarCargando(true);

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        final String retoId = String.valueOf(it.getIdReto());

        // Aceptar el reto (esto crea la sesión del oponente)
        api.aceptarReto(retoId).enqueue(new Callback<AceptarRetoResponse>() {
            @Override public void onResponse(Call<AceptarRetoResponse> call, Response<AceptarRetoResponse> resp) {
                if (!isAdded()) return;
                mostrarCargando(false);

                if (resp.isSuccessful() && resp.body() != null) {
                    AceptarRetoResponse body = resp.body();

                    // ACTUALIZAR BADGE: reto aceptado, actualizar contador
                    actualizarBadgeRetosRecibidos();

                    // Si el estado es "en_curso" y tiene preguntas, lanzar el quiz INMEDIATAMENTE
                    if (body.reto != null && "en_curso".equalsIgnoreCase(body.reto.estado) 
                        && body.preguntas != null && !body.preguntas.isEmpty()
                        && body.sesiones != null && !body.sesiones.isEmpty()) {
                        // Lanzar el quiz directamente sin pasar por la sala de espera
                        lanzarQuizDirectamente(body, it);
                        return;
                    }
                    // Si no está listo aún, abrir sala de espera
                    abrirSalaDespuesDeAceptar(it);
                } else {
                    // Fallback con body {}
                    Map<String, Object> body = new HashMap<>();
                    api.aceptarRetoConBody(retoId, body).enqueue(new Callback<AceptarRetoResponse>() {
                        @Override public void onResponse(Call<AceptarRetoResponse> call2, Response<AceptarRetoResponse> resp2) {
                            if (!isAdded()) return;
                            mostrarCargando(false);
                            if (!resp2.isSuccessful() || resp2.body() == null) {
                                Toast.makeText(requireContext(), "No se pudo aceptar (" + resp2.code() + ")", Toast.LENGTH_LONG).show();
                                return;
                            }
                            AceptarRetoResponse body2 = resp2.body();
                            // Si el estado es "en_curso" y tiene preguntas, lanzar el quiz INMEDIATAMENTE
                            if (body2.reto != null && "en_curso".equalsIgnoreCase(body2.reto.estado) 
                                && body2.preguntas != null && !body2.preguntas.isEmpty()
                                && body2.sesiones != null && !body2.sesiones.isEmpty()) {
                                lanzarQuizDirectamente(body2, it);
                                return;
                            }
                            abrirSalaDespuesDeAceptar(it);
                        }
                        @Override public void onFailure(Call<AceptarRetoResponse> call2, Throwable t2) {
                            if (!isAdded()) return;
                            mostrarCargando(false);
                            Toast.makeText(requireContext(), "Error: " + t2.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override public void onFailure(Call<AceptarRetoResponse> call, Throwable t) {
                if (!isAdded()) return;
                mostrarCargando(false);
                // Fallback con body {} si la falla fue de red/protocolo
                Map<String, Object> body = new HashMap<>();
                api.aceptarRetoConBody(retoId, body).enqueue(new Callback<AceptarRetoResponse>() {
                    @Override public void onResponse(Call<AceptarRetoResponse> call2, Response<AceptarRetoResponse> resp2) {
                        if (!isAdded()) return;
                        mostrarCargando(false);
                        if (!resp2.isSuccessful() || resp2.body() == null) {
                            Toast.makeText(requireContext(), "No se pudo aceptar (" + resp2.code() + ")", Toast.LENGTH_LONG).show();
                            return;
                        }
                        AceptarRetoResponse body2 = resp2.body();
                        // Si el estado es "en_curso" y tiene preguntas, lanzar el quiz INMEDIATAMENTE
                        if (body2.reto != null && "en_curso".equalsIgnoreCase(body2.reto.estado) 
                            && body2.preguntas != null && !body2.preguntas.isEmpty()
                            && body2.sesiones != null && !body2.sesiones.isEmpty()) {
                            lanzarQuizDirectamente(body2, it);
                            return;
                        }
                        abrirSalaDespuesDeAceptar(it);
                    }
                    @Override public void onFailure(Call<AceptarRetoResponse> call2, Throwable t2) {
                        if (!isAdded()) return;
                        mostrarCargando(false);
                        Toast.makeText(requireContext(), "Error: " + t2.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void lanzarQuizDirectamente(AceptarRetoResponse aceptar, RetoListItem it) {
        if (!isAdded() || getActivity() == null || aceptar == null) return;
        
        // Encontrar la sesión del usuario actual
        Integer myId = null;
        try {
            myId = TokenManager.getUserId(requireContext());
        } catch (Exception e) {
            // Si no se puede obtener el ID, abrir sala de espera como fallback
            abrirSalaDespuesDeAceptar(it);
            return;
        }
        
        int idSesion = aceptar.findSesionIdForUser(myId);
        if (idSesion <= 0) {
            // Si no se encuentra la sesión, abrir sala de espera como fallback
            abrirSalaDespuesDeAceptar(it);
            return;
        }
        
        // Preparar argumentos para FragmentQuiz
        Bundle args = new Bundle();
        args.putString("aceptarJson", new Gson().toJson(aceptar));
        args.putInt("idSesion", idSesion);
        args.putString("idReto", aceptar.reto != null ? String.valueOf(aceptar.reto.id_reto) : String.valueOf(it.getIdReto()));
        
        // Obtener nombre del oponente (creador)
        String nombreOponente = "Creador";
        if (aceptar.oponente != null && aceptar.oponente.nombre != null) {
            nombreOponente = aceptar.oponente.nombre;
        } else if (it.getCreador() != null && it.getCreador().getNombre() != null) {
            nombreOponente = it.getCreador().getNombre();
        }
        args.putString("opName", nombreOponente);
        
        // Crear y lanzar FragmentQuiz directamente
        FragmentQuiz f = new FragmentQuiz();
        f.setArguments(args);
        
        View overlay = getActivity().findViewById(R.id.container);
        if (overlay != null) overlay.setVisibility(View.VISIBLE);
        
        FragmentManager fm = getActivity().getSupportFragmentManager();
        fm.beginTransaction()
                .replace(overlay != null ? R.id.container : R.id.fragmentContainer, f)
                .addToBackStack("quiz")
                .commit();
    }
    
    private void abrirSalaDespuesDeAceptar(RetoListItem it) {
        // aceptarYIrSala ya llamó a aceptarReto, que creó sesiones para ambos y cambió el estado a 'en_curso'
        // Ahora solo debemos abrir la sala de espera, que detectará el estado 'en_curso' y comenzará el quiz inmediatamente
        if (!isAdded() || getActivity() == null) return;
        
        // Abrir FragmentLoadingSalaReto como RETADO (no creador)
        View overlay = getActivity().findViewById(R.id.container);
        if (overlay != null) overlay.setVisibility(View.VISIBLE);
        
        FragmentLoadingSalaReto f = FragmentLoadingSalaReto.newInstance(
                String.valueOf(it.getIdReto()),
                it.getArea() != null ? it.getArea() : "",
                it.getCreador() != null && it.getCreador().getNombre() != null ? it.getCreador().getNombre() : "Creador",
                false // esCreador = false
        );
        
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(overlay != null ? R.id.container : R.id.fragmentContainer, f)
                .addToBackStack("salaRetado")
                .commit();
    }

    /** Rechaza el reto y actualiza la lista */
    private void rechazarReto(RetoListItem it) {
        if (!isAdded()) return;

        if (it == null || it.getIdReto() == null) {
            Toast.makeText(requireContext(), "Reto inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        final String retoId = String.valueOf(it.getIdReto());

        // Llamar al endpoint de rechazar
        api.rechazarReto(retoId).enqueue(new Callback<BasicResponse>() {
            @Override
            public void onResponse(Call<BasicResponse> call, Response<BasicResponse> resp) {
                if (!isAdded()) return;

                if (resp.isSuccessful()) {
                    Toast.makeText(requireContext(), "Reto rechazado", Toast.LENGTH_SHORT).show();

                    // ACTUALIZAR BADGE: reto rechazado, actualizar contador inmediatamente
                    actualizarBadgeRetosRecibidos();

                    // Actualizar la lista para quitar el reto rechazado
                    cargar();
                    // También actualizar la lista de oponentes si hay un FragmentReto visible
                    actualizarListaOponentes();
                } else {
                    Toast.makeText(requireContext(), "No se pudo rechazar el reto", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BasicResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Error al rechazar: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Actualiza la lista de oponentes en FragmentReto si está visible */
    private void actualizarListaOponentes() {
        if (!isAdded() || getActivity() == null) return;

        // Usar Handler para actualizar después de un pequeño delay
        // Esto asegura que el backend haya procesado el rechazo antes de refrescar
        new Handler().postDelayed(() -> {
            if (!isAdded() || getActivity() == null) return;

            // Buscar FragmentReto en el fragment manager
            FragmentManager fm = getActivity().getSupportFragmentManager();
            
            // Buscar en todos los fragments del activity
            List<Fragment> fragments = fm.getFragments();
            for (Fragment f : fragments) {
                if (f instanceof FragmentReto && f.isAdded()) {
                    ((FragmentReto) f).cargarOponentes();
                    return;
                }
            }

            // También buscar en el parent fragment manager
            Fragment parent = getParentFragment();
            if (parent != null) {
                FragmentManager childFm = parent.getChildFragmentManager();
                List<Fragment> childFragments = childFm.getFragments();
                for (Fragment childF : childFragments) {
                    if (childF instanceof FragmentReto && childF.isAdded()) {
                        ((FragmentReto) childF).cargarOponentes();
                        return;
                    }
                }
            }
        }, 500); // 500ms de delay para asegurar que el backend procesó el rechazo
    }

    /**
     * Muestra un diálogo de confirmación para aceptar el reto
     * Este método se llama cuando el usuario toca la notificación
     */
    public void mostrarDialogoRetoDesdeNotificacion(String retoId, String retadorNombre, String area) {
        if (!isAdded() || getActivity() == null) {
            android.util.Log.w("FragmentRetosRecibidos", "⚠️ Fragment no está listo para mostrar diálogo");
            return;
        }

        android.util.Log.d("FragmentRetosRecibidos", "🎮 Mostrando diálogo para reto ID: " + retoId);

        // Buscar el reto en la lista actual
        RetoListItem retoEncontrado = null;
        if (adapter != null && adapter.getData() != null) {
            for (RetoListItem reto : adapter.getData()) {
                if (reto.getIdReto() != null && String.valueOf(reto.getIdReto()).equals(retoId)) {
                    retoEncontrado = reto;
                    break;
                }
            }
        }

        // Si no se encontró el reto en la lista actual, recargar primero
        if (retoEncontrado == null) {
            android.util.Log.d("FragmentRetosRecibidos", "🔄 Reto no encontrado en lista, recargando...");
            cargar();

            // Intentar de nuevo después de recargar
            new Handler().postDelayed(() -> {
                RetoListItem retoRecargado = null;
                if (adapter != null && adapter.getData() != null) {
                    for (RetoListItem reto : adapter.getData()) {
                        if (reto.getIdReto() != null && String.valueOf(reto.getIdReto()).equals(retoId)) {
                            retoRecargado = reto;
                            break;
                        }
                    }
                }

                if (retoRecargado != null) {
                    mostrarDialogoDeConfirmacion(retoRecargado, retadorNombre, area);
                } else {
                    android.util.Log.w("FragmentRetosRecibidos", "⚠️ Reto aún no encontrado después de recargar");
                    Toast.makeText(requireContext(), "Cargando reto...", Toast.LENGTH_SHORT).show();
                }
            }, 1000);
            return;
        }

        // Mostrar el diálogo inmediatamente
        mostrarDialogoDeConfirmacion(retoEncontrado, retadorNombre, area);
    }

    /**
     * Muestra el diálogo de confirmación con diseño atractivo
     */
    private void mostrarDialogoDeConfirmacion(RetoListItem reto, String retadorNombre, String area) {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        // Crear diálogo personalizado
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());

        // Inflar layout personalizado
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_aceptar_reto, null);

        // Configurar vistas del diálogo
        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloReto);
        TextView tvRetador = dialogView.findViewById(R.id.tvRetador);
        TextView tvArea = dialogView.findViewById(R.id.tvArea);
        android.widget.Button btnAceptar = dialogView.findViewById(R.id.btnAceptarReto);
        android.widget.Button btnRechazar = dialogView.findViewById(R.id.btnRechazarReto);

        // Configurar textos
        tvTitulo.setText("¡Tienes un nuevo reto!");
        tvRetador.setText("🎮 " + (retadorNombre != null ? retadorNombre : "Desconocido") + " te ha retado");
        tvArea.setText("📚 Área: " + (area != null ? area : "Desconocida"));

        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();

        // Hacer el fondo transparente para que se vea el diseño personalizado
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Configurar botón Aceptar
        btnAceptar.setOnClickListener(v -> {
            android.util.Log.d("FragmentRetosRecibidos", "✅ Usuario aceptó el reto");
            dialog.dismiss();
            aceptarYIrSala(reto);
        });

        // Configurar botón Rechazar
        btnRechazar.setOnClickListener(v -> {
            android.util.Log.d("FragmentRetosRecibidos", "❌ Usuario rechazó el reto");
            dialog.dismiss();
            rechazarReto(reto);
        });

        dialog.show();
        android.util.Log.d("FragmentRetosRecibidos", "✅ Diálogo mostrado correctamente");
    }

    private void mostrarCargando(boolean s) {
        // No mostrar el ProgressBar de carga (se ve feo según el usuario)
        if (pb != null) pb.setVisibility(View.GONE);
    }

    /**
     * Actualiza el badge de retos pendientes en el fragment padre
     * Se llama cuando se acepta o rechaza un reto para mantener el contador sincronizado
     */
    private void actualizarBadgeRetosRecibidos() {
        try {
            // Buscar el RetosFragment padre para actualizar el badge
            Fragment parentFragment = getParentFragment();
            while (parentFragment != null && !(parentFragment instanceof com.example.zavira_movil.ui.ranking.progreso.RetosFragment)) {
                parentFragment = parentFragment.getParentFragment();
            }

            if (parentFragment instanceof com.example.zavira_movil.ui.ranking.progreso.RetosFragment) {
                ((com.example.zavira_movil.ui.ranking.progreso.RetosFragment) parentFragment).refreshBadgeRetosRecibidos();
                android.util.Log.d("FragmentRetosRecibidos", "✅ Badge actualizado después de acción de reto");
            } else {
                android.util.Log.w("FragmentRetosRecibidos", "⚠️ RetosFragment padre no encontrado para actualizar badge");
            }
        } catch (Exception e) {
            android.util.Log.e("FragmentRetosRecibidos", "❌ Error al actualizar badge: " + e.getMessage());
        }
    }
}
