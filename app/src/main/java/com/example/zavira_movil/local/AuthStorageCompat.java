package com.example.zavira_movil.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Escribe el token en varios nombres de SharedPreferences y keys comunes,
 * para que cualquier interceptor ya existente lo pueda leer sin modificarlo.
 */
public final class AuthStorageCompat {

    // Conjuntos de prefs y keys "típicos" que suelen usar proyectos
    private static final String[] PREF_NAMES = new String[] {
            "ZAVIRA_PREFS",     // el tuyo actual
            "APP_PREFS",
            "prefs",
            "auth_prefs",
            "user_prefs"
    };

    private static final String[] TOKEN_KEYS = new String[] {
            "TOKEN",           // el tuyo actual
            "token",
            "auth_token",
            "jwt",
            "access_token",
            "Authorization"    // algunos guardan así
    };

    private AuthStorageCompat(){}

    public static void saveTokenEverywhere(Context ctx, String token) {
        if (ctx == null || token == null) return;

        for (String prefName : PREF_NAMES) {
            SharedPreferences sp = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            for (String key : TOKEN_KEYS) {
                ed.putString(key, token);
            }
            ed.apply();
        }
        Log.d("AuthStorageCompat", "Token escrito en múltiples prefs/keys para compatibilidad");
    }

    public static void clearAllTokens(Context ctx) {
        for (String prefName : PREF_NAMES) {
            SharedPreferences sp = ctx.getSharedPreferences(prefName, Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            for (String key : TOKEN_KEYS) ed.remove(key);
            ed.apply();
        }
    }
}
