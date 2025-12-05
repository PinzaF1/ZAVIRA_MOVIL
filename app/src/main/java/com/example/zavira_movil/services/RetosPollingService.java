package com.example.zavira_movil.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.notifications.NotificationItem;
import com.example.zavira_movil.notifications.NotificationStorage;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import java.util.HashSet;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Servicio alternativo para detectar nuevos retos mediante polling
 * Usado como fallback cuando FCM no funciona correctamente
 */
public class RetosPollingService extends Service {

    private static final String TAG = "RetosPolling";
    private static final String CHANNEL_ID = "retos_polling_channel";
    private static final int POLLING_INTERVAL_MS = 30000; // 30 segundos
    private static final long RETO_EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 horas

    private Handler handler;
    private Runnable pollingRunnable;
    private Set<String> retosYaNotificados;
    private SharedPreferences retosTimestampPrefs;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "✅ RetosPollingService creado");

        // Cargar IDs de retos ya notificados
        SharedPreferences prefs = getSharedPreferences("retos_polling_prefs", MODE_PRIVATE);
        retosYaNotificados = new HashSet<>(prefs.getStringSet("retos_notificados", new HashSet<>()));

        // Cargar timestamps de notificaciones
        retosTimestampPrefs = getSharedPreferences("retos_timestamps", MODE_PRIVATE);

        // 🔥 LIMPIAR RETOS EXPIRADOS (más de 24 horas)
        limpiarRetosExpirados();

        handler = new Handler(Looper.getMainLooper());

        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                verificarNuevosRetos();
                handler.postDelayed(this, POLLING_INTERVAL_MS);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🔄 RetosPollingService iniciado");

        // Crear canal de notificaciones
        crearCanalNotificaciones();

        // Iniciar polling
        handler.post(pollingRunnable);

        return START_STICKY; // Se reinicia automáticamente si el sistema lo mata
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "⚠️ RetosPollingService destruido");
        if (handler != null && pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // No necesitamos binding
    }

    /**
     * Verifica si hay nuevos retos recibidos
     */
    private void verificarNuevosRetos() {
        try {
            Log.d(TAG, "🔍 Verificando nuevos retos...");

            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            Call<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> call =
                apiService.listarRetos("recibidos");

            call.enqueue(new Callback<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>>() {
                @Override
                public void onResponse(Call<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> call,
                                     Response<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem> retos = response.body();
                            Log.d(TAG, "✅ Respuesta recibida: " + retos.size() + " retos");

                            int nuevosRetosCount = 0;

                            for (com.example.zavira_movil.retos1vs1.RetoListItem reto : retos) {
                                String retoId = String.valueOf(reto.getIdReto());
                                String estado = reto.getEstado();

                                // Solo notificar retos pendientes que no hemos notificado antes
                                if ("pendiente".equals(estado) && !retosYaNotificados.contains(retoId)) {
                                    String area = reto.getArea();

                                    com.example.zavira_movil.retos1vs1.RetoListItem.Creador creador = reto.getCreador();
                                    String retadorNombre = creador != null ? creador.getNombre() : "Desconocido";
                                    String retadorId = creador != null ? String.valueOf(creador.getIdUsuario()) : null;

                                    Log.d(TAG, "🎮 Nuevo reto detectado:");
                                    Log.d(TAG, "  • ID: " + retoId);
                                    Log.d(TAG, "  • Retador: " + retadorNombre);
                                    Log.d(TAG, "  • Área: " + area);

                                    // Guardar en historial de notificaciones
                                    guardarNotificacion(retoId, retadorNombre, retadorId, area);

                                    // Mostrar notificación local
                                    mostrarNotificacionLocal(retoId, retadorNombre, area);

                                    // Marcar como notificado
                                    retosYaNotificados.add(retoId);
                                    guardarRetosNotificados();

                                    // 🔥 GUARDAR TIMESTAMP DE LA NOTIFICACIÓN
                                    retosTimestampPrefs.edit()
                                        .putLong("reto_" + retoId, System.currentTimeMillis())
                                        .apply();

                                    nuevosRetosCount++;
                                }
                            }

                            if (nuevosRetosCount > 0) {
                                Log.d(TAG, "✅ " + nuevosRetosCount + " nuevos retos detectados y notificados");

                                // Enviar broadcast para actualizar badge
                                Intent broadcastIntent = new Intent("com.example.zavira_movil.UPDATE_RETOS_BADGE");
                                broadcastIntent.putExtra("nuevos_retos", nuevosRetosCount);
                                androidx.localbroadcastmanager.content.LocalBroadcastManager
                                    .getInstance(RetosPollingService.this).sendBroadcast(broadcastIntent);
                            } else {
                                Log.d(TAG, "ℹ️ No hay nuevos retos");
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error al procesar respuesta de retos", e);
                        }
                    } else {
                        Log.e(TAG, "❌ Error en respuesta: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<java.util.List<com.example.zavira_movil.retos1vs1.RetoListItem>> call, Throwable t) {
                    Log.e(TAG, "❌ Error de red al verificar retos", t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al verificar nuevos retos", e);
        }
    }

    /**
     * Guarda la notificación en el historial local
     */
    private void guardarNotificacion(String retoId, String retadorNombre, String retadorId, String area) {
        try {
            NotificationStorage storage = new NotificationStorage(this);

            NotificationItem item = new NotificationItem(
                "¡Nuevo Reto!",
                "Te han retado en " + area,
                "reto_recibido",
                area,
                null,
                System.currentTimeMillis(),
                retadorId,
                retadorNombre,
                null,
                retoId
            );

            storage.saveNotification(item);
            Log.d(TAG, "💾 Notificación guardada en historial");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al guardar notificación en historial", e);
        }
    }

    /**
     * Muestra una notificación local en el sistema
     */
    private void mostrarNotificacionLocal(String retoId, String retadorNombre, String area) {
        try {
            Intent intent = new Intent(this, HomeActivity.class);

            // Indicar que debe abrir la pestaña de Retos
            intent.putExtra("open_tab", "retos");

            // Indicar que debe mostrar específicamente la pestaña "Recibidos" (índice 1)
            intent.putExtra("retos_tab_index", 1);

            // NUEVO: Pasar el ID del reto para abrir el diálogo automáticamente
            intent.putExtra("reto_id", retoId);
            intent.putExtra("retador_nombre", retadorNombre);
            intent.putExtra("area", area);

            // Flags para navegación correcta
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            Log.d(TAG, "📱 Intent configurado para abrir Retos > Recibidos");
            Log.d(TAG, "📱 Reto ID: " + retoId + " para abrir diálogo");

            PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                retoId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.iconoeduexce)
                .setContentTitle("🎮 " + retadorNombre + " te ha retado")
                .setContentText("Área: " + area)
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText("¡" + retadorNombre + " te ha desafiado a un reto en " + area + "! Acepta el desafío."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(0xFF8B5CF6) // Morado para retos
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 250, 250, 250})
                .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(retoId.hashCode(), builder.build());

            Log.d(TAG, "🔔 Notificación local mostrada");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al mostrar notificación local", e);
        }
    }

    /**
     * Crea el canal de notificaciones para Android O+
     */
    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Notificaciones de Retos",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificaciones cuando recibes nuevos retos");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(0xFF8B5CF6); // Morado

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Guarda los IDs de retos ya notificados en SharedPreferences
     */
    private void guardarRetosNotificados() {
        SharedPreferences prefs = getSharedPreferences("retos_polling_prefs", MODE_PRIVATE);
        prefs.edit()
            .putStringSet("retos_notificados", retosYaNotificados)
            .apply();
    }

    /**
     * 🔥 LIMPIA RETOS EXPIRADOS (más de 24 horas desde la notificación)
     * Esto evita que se acumulen retos indefinidamente y previene notificaciones duplicadas
     */
    private void limpiarRetosExpirados() {
        long currentTime = System.currentTimeMillis();
        Set<String> retosExpirados = new HashSet<>();

        // Verificar cada reto notificado
        for (String retoId : retosYaNotificados) {
            long timestamp = retosTimestampPrefs.getLong("reto_" + retoId, 0);

            if (timestamp > 0) {
                long tiempoTranscurrido = currentTime - timestamp;

                // Si pasaron más de 24 horas, marcar como expirado
                if (tiempoTranscurrido > RETO_EXPIRATION_TIME) {
                    retosExpirados.add(retoId);
                    Log.d(TAG, "🗑️ Reto " + retoId + " expirado (más de 24h), limpiando...");
                }
            }
        }

        // Eliminar retos expirados
        if (!retosExpirados.isEmpty()) {
            retosYaNotificados.removeAll(retosExpirados);

            // Limpiar timestamps
            SharedPreferences.Editor editor = retosTimestampPrefs.edit();
            for (String retoId : retosExpirados) {
                editor.remove("reto_" + retoId);
            }
            editor.apply();

            // Guardar cambios
            guardarRetosNotificados();

            Log.d(TAG, "✅ " + retosExpirados.size() + " retos expirados eliminados");
        } else {
            Log.d(TAG, "ℹ️ No hay retos expirados para limpiar");
        }
    }
}


