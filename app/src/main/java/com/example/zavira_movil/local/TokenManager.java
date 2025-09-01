package com.example.zavira_movil.local;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREFS = "ZAVIRA_PREFS";
    private static final String KEY_TOKEN = "TOKEN";
    private final SharedPreferences prefs;

    public TokenManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clear() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }


    private static final String KEY_USER_ID = "user_id";

    public static void saveToken(Context ctx, String token) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public static String getToken(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_TOKEN, null);
    }

    public static void saveUserId(Context ctx, int id) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putInt(KEY_USER_ID, id).apply();
    }

    public static int getUserId(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_USER_ID, -1);
    }

    public static void clear(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
