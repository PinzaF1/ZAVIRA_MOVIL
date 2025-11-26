package com.example.zavira_movil.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;
import com.example.zavira_movil.notifications.GroupedNotificationsAdapter;
import com.example.zavira_movil.notifications.NotificationItem;
import com.example.zavira_movil.notifications.NotificationStorage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Calendar;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayout emptyState;
    private LinearLayout actionBar;
    private GroupedNotificationsAdapter adapter;
    private NotificationStorage notificationStorage;
    private MaterialButton btnClearOld;
    private MaterialButton btnMarkAllAsRead;
    private TextView tvNotificationCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Inicializar vistas
        ImageView btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerViewNotifications);
        emptyState = findViewById(R.id.emptyState);
        actionBar = findViewById(R.id.actionBar);
        btnClearOld = findViewById(R.id.btnClearOld);
        btnMarkAllAsRead = findViewById(R.id.btnMarkAllAsRead);
        tvNotificationCount = findViewById(R.id.tvNotificationCount);

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> {
            finish();
            sendBadgeUpdateBroadcast();
        });

        // Inicializar NotificationStorage
        notificationStorage = new NotificationStorage(this);

        // Configurar botones de acción
        setupActionButtons();

        // Configurar RecyclerView con swipe para eliminar
        setupRecyclerView();

        // Cargar notificaciones
        loadNotifications();
    }

    private void setupActionButtons() {
        // Botón "Limpiar notificaciones antiguas"
        btnClearOld.setOnClickListener(v -> clearOldNotifications());

        // Botón "Marcar todas como leídas"
        btnMarkAllAsRead.setOnClickListener(v -> markAllAsRead());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Crear adaptador con listeners
        adapter = new GroupedNotificationsAdapter(
            // Click listener
            (notification, originalPosition) -> {
                // Marcar como leída al hacer clic
                if (!notification.isRead()) {
                    notificationStorage.markAsRead(originalPosition);
                    notification.setRead(true);
                    loadNotifications(); // Recargar para actualizar UI
                    sendBadgeUpdateBroadcast();
                }
            },
            // Delete listener
            originalPosition -> {
                // Eliminar del storage
                notificationStorage.deleteNotification(originalPosition);
                sendBadgeUpdateBroadcast();
            }
        );

        recyclerView.setAdapter(adapter);

        // Configurar swipe para eliminar
        setupSwipeToDelete();
    }

    /**
     * Configura el swipe hacia la izquierda para eliminar notificaciones
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();

                // Eliminar de la vista
                adapter.removeNotification(position);

                // Mostrar Snackbar con opción de deshacer
                Snackbar.make(recyclerView, "Notificación eliminada", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer", v -> {
                        // Recargar todas las notificaciones (cancelar eliminación)
                        loadNotifications();
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            // Si no se deshizo la acción, actualizar contador
                            if (event != DISMISS_EVENT_ACTION) {
                                updateNotificationCount();
                                updateActionBarVisibility();
                                sendBadgeUpdateBroadcast();
                            }
                        }
                    })
                    .show();
            }

            @Override
            public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // Solo permitir swipe en notificaciones, no en encabezados de fecha
                if (viewHolder instanceof GroupedNotificationsAdapter.DateHeaderViewHolder) {
                    return 0; // No permitir swipe
                }
                return super.getSwipeDirs(recyclerView, viewHolder);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void loadNotifications() {
        android.util.Log.d("NotificationsActivity", "📋 loadNotifications() - Cargando notificaciones...");

        List<NotificationItem> notifications = notificationStorage.getAllNotifications();

        android.util.Log.d("NotificationsActivity", "  • Total notificaciones: " + notifications.size());

        updateNotificationCount();

        if (notifications.isEmpty()) {
            android.util.Log.d("NotificationsActivity", "  ⚠️ No hay notificaciones, mostrando empty state");
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            actionBar.setVisibility(View.GONE);
        } else {
            android.util.Log.d("NotificationsActivity", "  ✅ Mostrando notificaciones agrupadas por fecha");
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);

            // Mostrar barra de acciones si hay notificaciones
            actionBar.setVisibility(View.VISIBLE);

            // Actualizar visibilidad de botones
            updateActionBarVisibility();

            // Actualizar adaptador
            adapter.setNotifications(notifications);
        }
    }

    /**
     * Elimina notificaciones antiguas (más de 7 días)
     */
    private void clearOldNotifications() {
        List<NotificationItem> notifications = notificationStorage.getAllNotifications();

        // Calcular timestamp de hace 7 días
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        long sevenDaysAgo = calendar.getTimeInMillis();

        int deletedCount = 0;

        // Eliminar desde el final para no afectar índices
        for (int i = notifications.size() - 1; i >= 0; i--) {
            NotificationItem notification = notifications.get(i);
            if (notification.getTimestamp() < sevenDaysAgo) {
                notificationStorage.deleteNotification(i);
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            loadNotifications();
            sendBadgeUpdateBroadcast();

            // Mostrar feedback
            Snackbar.make(recyclerView, deletedCount + " notificaciones antiguas eliminadas", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(recyclerView, "No hay notificaciones antiguas", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Marca todas las notificaciones como leídas
     */
    private void markAllAsRead() {
        notificationStorage.markAllAsRead();
        loadNotifications();
        sendBadgeUpdateBroadcast();

        // Mostrar feedback
        Snackbar.make(recyclerView, "Todas las notificaciones marcadas como leídas", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Actualiza el contador de notificaciones
     */
    private void updateNotificationCount() {
        if (tvNotificationCount != null) {
            int unreadCount = notificationStorage.getUnreadCount();

            if (unreadCount > 0) {
                tvNotificationCount.setVisibility(View.VISIBLE);
                tvNotificationCount.setText(unreadCount + " sin leer");
            } else {
                tvNotificationCount.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Actualiza la visibilidad de los botones en la barra de acciones
     */
    private void updateActionBarVisibility() {
        int unreadCount = notificationStorage.getUnreadCount();
        List<NotificationItem> notifications = notificationStorage.getAllNotifications();

        // Calcular si hay notificaciones antiguas
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        long sevenDaysAgo = calendar.getTimeInMillis();

        boolean hasOldNotifications = false;
        for (NotificationItem notification : notifications) {
            if (notification.getTimestamp() < sevenDaysAgo) {
                hasOldNotifications = true;
                break;
            }
        }

        // Mostrar/ocultar botones
        btnMarkAllAsRead.setEnabled(unreadCount > 0);
        btnMarkAllAsRead.setAlpha(unreadCount > 0 ? 1.0f : 0.5f);

        btnClearOld.setEnabled(hasOldNotifications);
        btnClearOld.setAlpha(hasOldNotifications ? 1.0f : 0.5f);
    }

    /**
     * Envía broadcast para actualizar el badge en HomeActivity
     */
    private void sendBadgeUpdateBroadcast() {
        Intent intent = new Intent("com.example.zavira_movil.UPDATE_NOTIFICATION_BADGE");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }
}

