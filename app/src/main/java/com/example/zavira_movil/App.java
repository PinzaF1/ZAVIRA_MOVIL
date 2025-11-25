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
        } catch (Exception e) {
            Log.w("AppInit", "No pude inicializar FirebaseApp", e);
        }
    }
}

