package com.example.zavira_movil.ui.ranking.progreso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.zavira_movil.R;
import com.example.zavira_movil.retos1vs1.RetosTabsAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class RetosFragment extends Fragment {

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el MISMO layout de la antigua Activity (mantiene el diseño 1:1)
        return inflater.inflate(R.layout.activity_retosactivity, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);

        TabLayout tabLayout = v.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = v.findViewById(R.id.viewPager);
        View overlayContainer = v.findViewById(R.id.container);
        
        // Ocultar logo EduExce y campana en la Activity principal
        v.post(() -> ocultarTopBar());
        
        // CRÍTICO: Establecer el color naranja de la status bar cuando el fragment se crea
        if (getActivity() != null) {
            v.post(() -> {
                establecerStatusBarNaranja();
            });
        }

        // Adapter con constructor que acepta Fragment (ver RetosTabsAdapter abajo)
        viewPager.setAdapter(new RetosTabsAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Crear Reto");
            } else {
                // Pestaña Recibidos con layout personalizado para mostrar badge
                View customView = LayoutInflater.from(getContext()).inflate(R.layout.tab_recibidos_with_badge, null);
                tab.setCustomView(customView);

                // Inicializar con badge oculto
                updateRecibidosBadge(customView, 0);
            }
        }).attach();

        // Cargar contador de retos pendientes después de configurar las pestañas
        viewPager.post(() -> cargarContadorRetosPendientes());

        // Agregar listener para actualizar badge cuando se cambia de pestaña
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Si se cambia a la pestaña de Recibidos (posición 1), actualizar badge
                if (position == 1) {
                    viewPager.postDelayed(() -> cargarContadorRetosPendientes(), 300);
                }
            }
        });

        // Verificar si hay un índice de tab inicial específico (desde notificación)
        if (getArguments() != null) {
            int initialTabIndex = getArguments().getInt("initial_tab_index", -1);
            if (initialTabIndex >= 0 && initialTabIndex < 2) {
                android.util.Log.d("RetosFragment", "📱 Cambiando a tab con índice: " + initialTabIndex);

                // Usar post para asegurar que el ViewPager esté completamente inicializado
                viewPager.post(() -> {
                    viewPager.setCurrentItem(initialTabIndex, false);
                    android.util.Log.d("RetosFragment", "✅ Tab cambiado a índice: " + initialTabIndex);

                    // NUEVO: Si hay un ID de reto, notificar al fragment de Recibidos
                    String retoId = getArguments().getString("reto_id");
                    if (retoId != null && initialTabIndex == 1) {
                        android.util.Log.d("RetosFragment", "📱 Notificando al fragment Recibidos sobre reto ID: " + retoId);

                        // Esperar un poco más para que el fragment de Recibidos esté completamente cargado
                        viewPager.postDelayed(() -> {
                            notificarFragmentRecibidos(retoId,
                                getArguments().getString("retador_nombre"),
                                getArguments().getString("area"));
                        }, 500);
                    }
                });
            }
        }

        // Mostrar/ocultar overlay según backstack de este fragment (hijos)
        if (overlayContainer != null) {
            getChildFragmentManager().addOnBackStackChangedListener(() -> {
                boolean hasBackStack = getChildFragmentManager().getBackStackEntryCount() > 0;
                overlayContainer.setVisibility(hasBackStack ? View.VISIBLE : View.GONE);
            });
        }
    }
    
    /**
     * Notifica al fragment de Recibidos para que muestre el diálogo del reto
     */
    private void notificarFragmentRecibidos(String retoId, String retadorNombre, String area) {
        try {
            android.util.Log.d("RetosFragment", "🔍 Buscando fragment Recibidos para mostrar diálogo");

            // ViewPager2 almacena fragments de manera diferente
            // Intentar obtenerlo directamente del adapter
            ViewPager2 viewPager = getView().findViewById(R.id.viewPager);

            if (viewPager != null && viewPager.getAdapter() != null) {
                // Asegurarnos de estar en el tab correcto (índice 1 = Recibidos)
                if (viewPager.getCurrentItem() != 1) {
                    android.util.Log.d("RetosFragment", "📱 Cambiando a tab Recibidos");
                    viewPager.setCurrentItem(1, false);
                }

                // Esperar un poco más para que el fragment esté completamente cargado
                viewPager.postDelayed(() -> {
                    // Buscar todos los fragments hijo
                    java.util.List<Fragment> fragments = getChildFragmentManager().getFragments();
                    android.util.Log.d("RetosFragment", "📋 Total fragments encontrados: " + fragments.size());

                    for (Fragment f : fragments) {
                        android.util.Log.d("RetosFragment", "🔎 Fragment: " + f.getClass().getSimpleName());

                        if (f instanceof com.example.zavira_movil.retos1vs1.FragmentRetosRecibidos && f.isAdded()) {
                            android.util.Log.d("RetosFragment", "✅ Fragment Recibidos encontrado, mostrando diálogo");

                            ((com.example.zavira_movil.retos1vs1.FragmentRetosRecibidos) f)
                                .mostrarDialogoRetoDesdeNotificacion(retoId, retadorNombre, area);
                            return;
                        }
                    }

                    android.util.Log.w("RetosFragment", "⚠️ Fragment Recibidos no encontrado en la lista");
                }, 800); // Aumentar delay a 800ms
            } else {
                android.util.Log.w("RetosFragment", "⚠️ ViewPager no encontrado o sin adapter");
            }
        } catch (Exception e) {
            android.util.Log.e("RetosFragment", "❌ Error al notificar fragment Recibidos", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Asegurar que el topBar esté oculto cada vez que el fragmento se muestra
        if (getView() != null && isAdded()) {
            getView().post(() -> ocultarTopBar());
        }
        
        // CRÍTICO: Establecer el color naranja de la status bar cuando el fragment está visible
        if (getActivity() != null && isAdded()) {
            // Asegurar que la status bar sea naranja para Retos
            getActivity().runOnUiThread(() -> {
                establecerStatusBarNaranja();
            });
        }

        // Actualizar contador de retos pendientes cuando el usuario regresa a la pantalla
        if (getView() != null && isAdded()) {
            getView().post(() -> cargarContadorRetosPendientes());
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        // También establecer el color cuando el fragment inicia
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> {
                establecerStatusBarNaranja();
            });
        }
    }
    
    /**
     * Establece el color blanco de la status bar para Retos
     */
    private void establecerStatusBarNaranja() {
        if (getActivity() == null || !isAdded()) {
            return;
        }
        
        android.view.Window window = getActivity().getWindow();
        if (window == null) {
            return;
        }
        
        int blancoColor = android.graphics.Color.WHITE;
        
        // CRÍTICO: Limpiar TODAS las flags que puedan interferir
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        
        // Establecer el color blanco INMEDIATAMENTE
        window.setStatusBarColor(blancoColor);
        
        // Obtener las flags actuales del sistema UI
        int currentFlags = window.getDecorView().getSystemUiVisibility();
        
        // Configurar el estilo del texto (oscuro para fondo blanco)
        // IMPORTANTE: NO usar LAYOUT_FULLSCREEN porque oculta el color de la status bar
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            androidx.core.view.WindowInsetsControllerCompat windowInsetsController = 
                androidx.core.view.WindowCompat.getInsetsController(window, window.getDecorView());
            if (windowInsetsController != null) {
                // Para fondo blanco, usar texto oscuro
                windowInsetsController.setAppearanceLightStatusBars(true);
            }
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            // Limpiar todas las flags problemáticas
            int newFlags = currentFlags;
            // Activar LIGHT_STATUS_BAR (texto oscuro para fondo blanco)
            newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            // Eliminar LAYOUT_FULLSCREEN (permite que el contenido se dibuje detrás de la status bar, ocultando el color)
            newFlags = newFlags & ~android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            // Mantener LAYOUT_STABLE para estabilidad
            newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            // Mantener las flags de navegación si existen
            if ((currentFlags & android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) != 0) {
                newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            if ((currentFlags & android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) != 0) {
                newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if ((currentFlags & android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) != 0) {
                newFlags = newFlags | android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            window.getDecorView().setSystemUiVisibility(newFlags);
        }
        
        // Forzar aplicación múltiple del color con diferentes delays
        window.getDecorView().post(() -> {
            window.setStatusBarColor(blancoColor);
            android.util.Log.d("RetosFragment", "Post 0ms: Status bar color establecido a BLANCO");
        });
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
            int actualColor = window.getStatusBarColor();
            android.util.Log.d("RetosFragment", "Post 50ms: Status bar color verificado: " + 
                String.format("#%08X", actualColor));
        }, 50);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
            android.util.Log.d("RetosFragment", "Post 100ms: Status bar color re-establecido");
        }, 100);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
            int actualColor = window.getStatusBarColor();
            android.util.Log.d("RetosFragment", "Post 300ms: Status bar color final verificado: " + 
                String.format("#%08X", actualColor) + " (esperado: BLANCO)");
        }, 300);
        
        window.getDecorView().postDelayed(() -> {
            window.setStatusBarColor(blancoColor);
        }, 500);
        
        android.util.Log.d("RetosFragment", "Status bar configurada a BLANCO desde fragment");
    }
    
    @Override
    public void onDestroyView() {
        // NO restaurar el topBar aquí porque puede estar navegando a otra Activity (Perfil)
        // El topBar solo debe restaurarse cuando cambias a otro fragment dentro de HomeActivity
        // HomeActivity se encargará de restaurarlo cuando sea necesario
        super.onDestroyView();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        // NO restaurar el topBar en onPause
        // ProfileActivity es una Activity separada, así que HomeActivity queda en segundo plano
        // Si restauramos aquí, el topBar aparecería en segundo plano
    }
    
    /**
     * Actualiza el badge de la pestaña Recibidos con el número de retos pendientes
     */
    private void updateRecibidosBadge(View customView, int count) {
        if (customView == null) return;

        android.widget.TextView tvBadgeCount = customView.findViewById(R.id.tvBadgeCount);
        android.widget.TextView tvTabText = customView.findViewById(R.id.tvTabText);

        if (tvBadgeCount != null) {
            if (count > 0) {
                tvBadgeCount.setText(String.valueOf(count));
                tvBadgeCount.setVisibility(android.view.View.VISIBLE);
                android.util.Log.d("RetosFragment", "✅ Badge actualizado: " + count + " retos pendientes");
            } else {
                tvBadgeCount.setVisibility(android.view.View.GONE);
                android.util.Log.d("RetosFragment", "❌ Badge ocultado: no hay retos pendientes");
            }
        }

        // Asegurar que el texto del tab esté visible
        if (tvTabText != null) {
            tvTabText.setText("Recibidos");
        }
    }

    /**
     * Carga el número de retos pendientes desde el servidor y actualiza el badge
     */
    private void cargarContadorRetosPendientes() {
        if (getContext() == null) return;

        com.example.zavira_movil.remote.ApiService api =
            com.example.zavira_movil.remote.RetrofitClient.getInstance()
                .create(com.example.zavira_movil.remote.ApiService.class);

        retrofit2.Call<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> call =
            api.listarRetos("recibidos");

        call.enqueue(new retrofit2.Callback<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> call,
                                 retrofit2.Response<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> resp) {
                if (!isAdded() || getView() == null) return;

                int count = 0;
                if (resp.isSuccessful() && resp.body() != null) {
                    count = resp.body().size();
                }

                android.util.Log.d("RetosFragment", "📊 Retos pendientes encontrados: " + count);

                // Actualizar badge en el tab de Recibidos
                TabLayout tabLayout = getView().findViewById(R.id.tabLayout);
                if (tabLayout != null && tabLayout.getTabCount() > 1) {
                    TabLayout.Tab recibidosTab = tabLayout.getTabAt(1);
                    if (recibidosTab != null && recibidosTab.getCustomView() != null) {
                        updateRecibidosBadge(recibidosTab.getCustomView(), count);
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> call,
                                Throwable t) {
                android.util.Log.w("RetosFragment", "Error al cargar contador de retos: " + t.getMessage());

                // En caso de error, ocultar badge
                if (isAdded() && getView() != null) {
                    TabLayout tabLayout = getView().findViewById(R.id.tabLayout);
                    if (tabLayout != null && tabLayout.getTabCount() > 1) {
                        TabLayout.Tab recibidosTab = tabLayout.getTabAt(1);
                        if (recibidosTab != null && recibidosTab.getCustomView() != null) {
                            updateRecibidosBadge(recibidosTab.getCustomView(), 0);
                        }
                    }
                }
            }
        });
    }

    private void ocultarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.GONE);
            }
        }
    }
    
    private void restaurarTopBar() {
        if (getActivity() != null && isAdded()) {
            View topBar = getActivity().findViewById(R.id.topBar);
            if (topBar != null) {
                topBar.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Método público para que otros componentes puedan actualizar el badge de retos pendientes
     * Útil cuando se acepta o rechaza un reto desde otra pantalla
     */
    public void refreshBadgeRetosRecibidos() {
        if (isAdded() && getView() != null) {
            cargarContadorRetosPendientes();
            android.util.Log.d("RetosFragment", "🔄 Badge de retos actualizado desde componente externo");
        }
    }
}
