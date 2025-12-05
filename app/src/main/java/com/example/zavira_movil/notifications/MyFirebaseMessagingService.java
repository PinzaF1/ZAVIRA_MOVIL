package com.example.zavira_movil.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "eduexce_notifications";
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        // LOG CRÍTICO: Confirmar que este método SE ESTÁ LLAMANDO
        Log.e(TAG, "⚡⚡⚡ onMessageReceived() LLAMADO ⚡⚡⚡");
        Log.e(TAG, "⚡ Timestamp: " + System.currentTimeMillis());
        Log.e(TAG, "⚡ Thread: " + Thread.currentThread().getName());

        Log.d(TAG, "========================================");
        Log.d(TAG, "🔔 NOTIFICACIÓN FCM RECIBIDA");
        Log.d(TAG, "========================================");
        Log.d(TAG, "De: " + remoteMessage.getFrom());
        Log.d(TAG, "ID Mensaje: " + remoteMessage.getMessageId());

        // DIAGNÓSTICO: Verificar si hay notificación Y datos
        boolean tieneNotificacion = remoteMessage.getNotification() != null;
        boolean tieneDatos = remoteMessage.getData() != null && !remoteMessage.getData().isEmpty();

        Log.d(TAG, "📊 TIPO DE MENSAJE:");
        Log.d(TAG, "  • Tiene 'notification': " + tieneNotificacion);
        Log.d(TAG, "  • Tiene 'data': " + tieneDatos);
        Log.d(TAG, "  • Tamaño de data: " + (remoteMessage.getData() != null ? remoteMessage.getData().size() : 0));
        Log.d(TAG, "----------------------------------------");

        // Verificar si el mensaje contiene datos
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "----------------------------------------");
            Log.d(TAG, "📦 DATOS COMPLETOS DEL MENSAJE:");
            Map<String, String> data = remoteMessage.getData();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                Log.d(TAG, "  • " + entry.getKey() + " = " + entry.getValue());
            }
            Log.d(TAG, "----------------------------------------");

            // Verificar campos específicos de reto
            String tipo = data.get("tipo");
            String retadorNombre = data.get("retador_nombre");
            String area = data.get("area");
            String retoId = data.get("reto_id");
            String retadorId = data.get("retador_id");

            Log.d(TAG, "🎮 CAMPOS DE RETO DETECTADOS:");
            Log.d(TAG, "  • tipo: " + tipo);
            Log.d(TAG, "  • retador_nombre: " + retadorNombre);
            Log.d(TAG, "  • area: " + area);
            Log.d(TAG, "  • reto_id: " + retoId);
            Log.d(TAG, "  • retador_id: " + retadorId);
            Log.d(TAG, "----------------------------------------");

            handleDataMessage(data);
        }
        
        // Verificar si el mensaje contiene una notificación
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "📬 NOTIFICACIÓN VISUAL:");
            Log.d(TAG, "  • Título: " + title);
            Log.d(TAG, "  • Cuerpo: " + body);
            Log.d(TAG, "----------------------------------------");

            // Guardar la notificación en el historial
            saveNotificationToHistory(title, body, remoteMessage.getData());
            
            sendNotification(title, body, remoteMessage.getData());
        }

        Log.d(TAG, "========================================");
    }
    
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Nuevo token FCM: " + token);
        
        // Guardar el token localmente
        saveTokenToPreferences(token);
        
        // Enviar el token al servidor
        sendTokenToServer(token);
    }
    
    private void handleDataMessage(Map<String, String> data) {
        Log.d(TAG, "🔄 handleDataMessage() - Procesando mensaje de datos");

        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("tipo");

        Log.d(TAG, "  • title extraído: " + title);
        Log.d(TAG, "  • message extraído: " + message);
        Log.d(TAG, "  • tipo extraído: " + type);

        if (title == null) {
            title = getString(R.string.notification_title);
            Log.d(TAG, "  ⚠️ Title era null, usando default: " + title);
        }
        if (message == null) {
            message = getString(R.string.notification_message);
            Log.d(TAG, "  ⚠️ Message era null, usando default: " + message);
        }

        Log.d(TAG, "  ✅ Guardando notificación en historial...");
        // Guardar la notificación en el historial
        saveNotificationToHistory(title, message, data);
        
        Log.d(TAG, "  ✅ Enviando notificación visual...");
        sendNotification(title, message, data);
    }
    
    private void sendNotification(String title, String messageBody, Map<String, String> data) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        // Agregar datos extras al intent
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // Determinar el color y estilo según el tipo de notificación
        int notificationColor = getNotificationColor(data);
        String notificationType = data != null ? data.get("tipo") : null;
        
        // Construir notificación con estilo expandido
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.iconoeduexce)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(messageBody)
                        .setBigContentTitle(title))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setColor(notificationColor)
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        // Agregar información adicional si está disponible
        if (data != null) {
            String area = data.get("area");
            String puntaje = data.get("puntaje");
            
            if (area != null && puntaje != null) {
                String infoLine = "📚 " + area + " • Puntaje: " + puntaje + "%";
                notificationBuilder.setSubText(infoLine);
            }
        }
        
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Crear canal de notificación para Android O y superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.default_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de EduExce");
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(notificationColor);
            notificationManager.createNotificationChannel(channel);
        }
        
        // Usar un ID único basado en el timestamp para permitir múltiples notificaciones
        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
    
    /**
     * Determina el color de la notificación según el tipo
     */
    private int getNotificationColor(Map<String, String> data) {
        if (data == null) {
            return 0xFF3B82F6; // Azul por defecto
        }
        
        String tipo = data.get("tipo");
        String puntajeStr = data.get("puntaje");
        
        // Color según el tipo de notificación
        if ("puntaje_bajo_inmediato".equals(tipo)) {
            return 0xFFEF4444; // Rojo para puntaje bajo
        } else if ("recordatorio_practica".equals(tipo)) {
            return 0xFFF59E0B; // Naranja para recordatorios
        } else if ("logro_desbloqueado".equals(tipo)) {
            return 0xFF22C55E; // Verde para logros
        } else if ("reto_recibido".equals(tipo)) {
            return 0xFF8B5CF6; // Morado para retos
        }
        
        // Color según el puntaje si está disponible
        if (puntajeStr != null) {
            try {
                int puntaje = Integer.parseInt(puntajeStr);
                if (puntaje < 40) {
                    return 0xFFEF4444; // Rojo
                } else if (puntaje < 70) {
                    return 0xFFF59E0B; // Naranja
                } else {
                    return 0xFF22C55E; // Verde
                }
            } catch (NumberFormatException e) {
                // Ignorar si no se puede parsear
            }
        }
        
        return 0xFF3B82F6; // Azul por defecto
    }
    
    /**
     * Guarda la notificación en el historial local
     */
    private void saveNotificationToHistory(String title, String message, Map<String, String> data) {
        try {
            Log.d(TAG, "💾 saveNotificationToHistory() - Iniciando...");
            Log.d(TAG, "  • title: " + title);
            Log.d(TAG, "  • message: " + message);

            NotificationStorage notificationStorage = new NotificationStorage(this);
            
            String tipo = data != null ? data.get("tipo") : null;
            String area = data != null ? data.get("area") : null;
            String puntaje = data != null ? data.get("puntaje") : null;
            
            // Campos específicos para retos
            String retadorId = data != null ? data.get("retador_id") : null;
            String retadorNombre = data != null ? data.get("retador_nombre") : null;
            String retadorFoto = data != null ? data.get("retador_foto") : null;
            String retoId = data != null ? data.get("reto_id") : null;

            Log.d(TAG, "  📊 Datos extraídos:");
            Log.d(TAG, "    • tipo: " + tipo);
            Log.d(TAG, "    • area: " + area);
            Log.d(TAG, "    • puntaje: " + puntaje);
            Log.d(TAG, "    • retador_id: " + retadorId);
            Log.d(TAG, "    • retador_nombre: " + retadorNombre);
            Log.d(TAG, "    • retador_foto: " + retadorFoto);
            Log.d(TAG, "    • reto_id: " + retoId);

            NotificationItem item;

            // Si es una notificación de reto, usar el constructor extendido
            if ("reto_recibido".equals(tipo) || retadorId != null) {
                Log.d(TAG, "  🎮 Detectado como RETO, usando constructor extendido");
                item = new NotificationItem(
                    title,
                    message,
                    tipo != null ? tipo : "reto_recibido",
                    area,
                    puntaje,
                    System.currentTimeMillis(),
                    retadorId,
                    retadorNombre,
                    retadorFoto,
                    retoId
                );
                Log.d(TAG, "  ✅ NotificationItem de RETO creado:");
                Log.d(TAG, "    • tipo: " + item.getTipo());
                Log.d(TAG, "    • retadorNombre: " + item.getRetadorNombre());
                Log.d(TAG, "    • area: " + item.getArea());
                Log.d(TAG, "    • retoId: " + item.getRetoId());
            } else {
                Log.d(TAG, "  📝 Detectado como notificación NORMAL");
                // Notificación normal (puntaje, logros, etc.)
                item = new NotificationItem(
                    title,
                    message,
                    tipo,
                    area,
                    puntaje,
                    System.currentTimeMillis()
                );
            }

            Log.d(TAG, "  💾 Guardando en NotificationStorage...");
            notificationStorage.saveNotification(item);
            Log.d(TAG, "  ✅ Notificación guardada exitosamente");
            Log.d(TAG, "  📊 Total de notificaciones: " + notificationStorage.getAllNotifications().size());
            Log.d(TAG, "  📊 Notificaciones no leídas: " + notificationStorage.getUnreadCount());

            // Enviar broadcast para actualizar el badge en HomeActivity
            Intent intent = new Intent("com.example.zavira_movil.UPDATE_NOTIFICATION_BADGE");
            androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error al guardar notificación en historial", e);
        }
    }
    
    private void saveTokenToPreferences(String token) {
        SharedPreferences prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE);
        prefs.edit().putString("fcm_token", token).apply();
    }
    
    private void sendTokenToServer(String token) {
        SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
        String authToken = prefs.getString("token", null);
        
        if (authToken == null) {
            Log.w(TAG, "Usuario no autenticado, no se puede enviar el token al servidor");
            return;
        }
        
        try {
            // Obtener device_id único del dispositivo
            String deviceId = Settings.Secure.getString(
                getContentResolver(), 
                Settings.Secure.ANDROID_ID
            );
            
            // Construir body según el formato esperado por el backend
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("token", token);
            jsonBody.put("device_id", deviceId);
            jsonBody.put("platform", "android");
            
            RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json")
            );
            
            ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);
            Call<Void> call = apiService.registerFCMToken(body);
            
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@androidx.annotation.NonNull Call<Void> call, @androidx.annotation.NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Token FCM registrado exitosamente en el servidor");
                    } else {
                        try {
                            String errorBody = response.errorBody() != null ? 
                                response.errorBody().string() : "Sin detalles";
                            Log.e(TAG, "❌ Error al registrar token FCM: " + response.code() + " - " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error al registrar token FCM: " + response.code());
                        }
                    }
                }
                
                @Override
                public void onFailure(@androidx.annotation.NonNull Call<Void> call, @androidx.annotation.NonNull Throwable t) {
                    Log.e(TAG, "❌ Fallo de red al enviar token FCM al servidor", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al preparar el token FCM para enviar", e);
        }
    }
}
