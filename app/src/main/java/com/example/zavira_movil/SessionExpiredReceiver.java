package com.example.zavira_movil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.zavira_movil.local.TokenManager;

public class SessionExpiredReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d("SessionExpiredReceiver", "Recibido broadcast de sesión expirada");
            // Limpieza local
            TokenManager.clearAll(context);
            // Lanzar LoginActivity como nueva task
            Intent i = new Intent(context, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
        } catch (Exception e) {
            Log.w("SessionExpiredReceiver", "Error manejando sesión expirada", e);
        }
    }
}
