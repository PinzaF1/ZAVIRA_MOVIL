package com.example.zavira_movil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.zavira_movil.Home.HomeActivity;
import com.example.zavira_movil.local.TokenManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 101;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔔 CRÍTICO: Solicitar permisos de notificaciones en Android 13+
        solicitarPermisosNotificaciones();

        TokenManager tokenManager = new TokenManager();
        String token = tokenManager.getToken(this);

        if (token != null && !token.isEmpty()) {
            // Si hay token → va al Home
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            // Si no hay token → va al Login
            startActivity(new Intent(this, LoginActivity.class));
        }

        // Cierra MainActivity para que no regrese con "back"
        finish();
    }

    /**
     * Solicita permisos de notificaciones para Android 13+ (API 33+)
     */
    private void solicitarPermisosNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                Log.w(TAG, "⚠️ Permiso POST_NOTIFICATIONS NO concedido - Solicitando...");

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_NOTIFICATION_PERMISSION
                );
            } else {
                Log.d(TAG, "✅ Permiso POST_NOTIFICATIONS YA concedido");
            }
        } else {
            Log.d(TAG, "✅ Android < 13 - Permiso POST_NOTIFICATIONS no requerido");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "✅ Usuario CONCEDIÓ permiso POST_NOTIFICATIONS");
            } else {
                Log.e(TAG, "❌ Usuario DENEGÓ permiso POST_NOTIFICATIONS");
                Log.e(TAG, "⚠️ Las notificaciones FCM NO funcionarán sin este permiso");
            }
        }
    }
}
