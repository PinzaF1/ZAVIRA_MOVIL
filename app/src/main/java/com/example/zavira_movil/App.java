package com.example.zavira_movil;

import android.app.Application;
import android.util.Log;

import com.example.zavira_movil.remote.RetrofitClient;
import com.google.firebase.FirebaseApp;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Inicializar RetrofitClient con contexto de aplicación para que los interceptors
        // puedan leer TokenManager y enviar broadcasts de sesión expirada.
        try {
            RetrofitClient.init(getApplicationContext());
        } catch (Exception e) {
            Log.w("AppInit", "No pude inicializar RetrofitClient", e);
        }

        // Inicializar Firebase (si google-services.json está presente)
        try {
            FirebaseApp.initializeApp(this);
            Log.d("AppInit", "✅ Firebase inicializado correctamente");

            // Obtener y mostrar el token FCM inmediatamente
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d("AppInit", "========================================");
                        Log.d("AppInit", "🔥 TOKEN FCM AL INICIAR APP");
                        Log.d("AppInit", "========================================");
                        Log.d("AppInit", "📱 Token completo: " + token);
                        Log.d("AppInit", "📏 Longitud: " + token.length() + " caracteres");
                        Log.d("AppInit", "🔑 Primeros 20: " + token.substring(0, Math.min(20, token.length())));
                        Log.d("AppInit", "========================================");

                        // Guardar en SharedPreferences
                        android.content.SharedPreferences prefs = getSharedPreferences("fcm_prefs", MODE_PRIVATE);
                        prefs.edit().putString("fcm_token", token).apply();
                        Log.d("AppInit", "💾 Token guardado en SharedPreferences");
                    } else {
                        Log.e("AppInit", "❌ ERROR al obtener token FCM:");
                        if (task.getException() != null) {
                            Log.e("AppInit", "  • Error: " + task.getException().getMessage());
                        }
                    }
                });
        } catch (Exception e) {
            Log.e("AppInit", "❌ No pude inicializar FirebaseApp", e);
        }
    }
}

