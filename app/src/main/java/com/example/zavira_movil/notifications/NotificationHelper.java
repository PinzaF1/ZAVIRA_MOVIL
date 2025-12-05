package com.example.zavira_movil.notifications;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import android.os.Handler;
import android.os.Looper;

import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationHelper {
    
    private static final String TAG = "NotificationHelper";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private Context context;
    
    public NotificationHelper(Context context) {
        this.context = context;
    }
    
    /**
     * Solicita permisos de notificaciones para Android 13+
     */
    public void requestNotificationPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_CODE
                );
            }
        }
    }
    
    /**
     * Verifica si los permisos de notificaciones están otorgados
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true; // En versiones anteriores a Android 13, no se necesita permiso
    }
    
    /**
     * Obtiene el token FCM actual
     */
    public void getCurrentToken(OnTokenReceivedListener listener) {
        FirebaseMessaging.getInstance().getToken()
            .addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(Task<String> task) {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Error al obtener el token FCM", task.getException());
                        listener.onTokenReceived(null);
                        return;
                    }
                    
                    String token = task.getResult();
                    Log.d(TAG, "Token FCM obtenido: " + token);
                    
                    // Guardar token en SharedPreferences
                    saveTokenToPreferences(token);
                    
                    listener.onTokenReceived(token);
                }
            });
    }
    
    /**
     * Guarda el token FCM en SharedPreferences
     */
    private void saveTokenToPreferences(String token) {
        SharedPreferences prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("fcm_token", token).apply();
    }
    
    /**
     * Obtiene el token FCM guardado en SharedPreferences
     */
    public String getSavedToken() {
        SharedPreferences prefs = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE);
        return prefs.getString("fcm_token", null);
    }
    
    /**
     * Interfaz para callback cuando se obtiene el token
     */
    public interface OnTokenReceivedListener {
        void onTokenReceived(String token);
    }

    /**
     * Suscribe al dispositivo a un topic de institución (ej: "institution_123").
     */
    public void subscribeToInstitutionTopic(int institutionId) {
        String topic = "institution_" + institutionId;
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) Log.d(TAG, "Subscribed to topic: " + topic);
                else Log.w(TAG, "Failed to subscribe to topic: " + topic, task.getException());
            });
    }

    /**
     * Desuscribe al dispositivo de un topic de institución
     */
    public void unsubscribeFromInstitutionTopic(int institutionId) {
        String topic = "institution_" + institutionId;
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) Log.d(TAG, "Unsubscribed from topic: " + topic);
                else Log.w(TAG, "Failed to unsubscribe from topic: " + topic, task.getException());
            });
    }

    /**
     * Envía el token al servidor con reintentos simples (exponencial).
     * Este método hace best-effort y guarda logs; si falla, reintentará hasta maxRetries.
     */
    public void sendTokenToServer(String token, int maxRetries) {
        sendTokenAttempt(token, 0, maxRetries);
    }

    private void sendTokenAttempt(String token, int attempt, int maxRetries) {
        if (token == null) return;

        try {
            String deviceId = android.provider.Settings.Secure.getString(
                context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
            );

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
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Token FCM registrado correctamente en servidor");
                    } else {
                        Log.e(TAG, "Error al registrar token (code=" + response.code() + ")");
                        if (attempt < maxRetries) scheduleRetry(token, attempt + 1, maxRetries);
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Fallo al enviar token FCM al servidor", t);
                    if (attempt < maxRetries) scheduleRetry(token, attempt + 1, maxRetries);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error preparando token FCM para enviar", e);
            if (attempt < maxRetries) scheduleRetry(token, attempt + 1, maxRetries);
        }
    }

    private void scheduleRetry(String token, int nextAttempt, int maxRetries) {
        long delayMs = (long) Math.pow(2, nextAttempt) * 1000L; // 2^n * 1s
        new Handler(Looper.getMainLooper()).postDelayed(() -> sendTokenAttempt(token, nextAttempt, maxRetries), delayMs);
    }

}
