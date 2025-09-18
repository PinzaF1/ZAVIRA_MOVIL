package com.example.zavira_movil.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenManager {

    private static final String PREFS = "ZAVIRA_PREFS";
    private static final String KEY_TOKEN = "TOKEN";
    private static final String KEY_USER_ID = "user_id";

    // ----- TOKEN -----
    public static void setToken(Context ctx, String token) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getString(KEY_TOKEN, null);
    }

    public static void clearToken(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_TOKEN).apply();
    }

    // ----- USER ID -----
    public static void setUserId(Context ctx, int id) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_USER_ID, id).apply();
    }

    public static int getUserId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getInt(KEY_USER_ID, -1);
    }

    public static void clearUserId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove(KEY_USER_ID).apply();
    }

    // ----- CLEAR ALL -----
    public static void clearAll(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }

    // ----- EXTRAER USER ID DEL JWT -----
    /**
     * Extrae el "id" del payload del JWT sin librerías externas.
     * Si no lo encuentra, retorna -1.
     */
    public static int extractUserIdFromJwt(String jwt) {
        if (jwt == null) return -1;
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) return -1;

            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE);
            String payload = new String(decoded, StandardCharsets.UTF_8);

            // Busca: "id": 123  ó  "id":123
            Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(payload);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception ignored) {}

        return -1;
    }
}