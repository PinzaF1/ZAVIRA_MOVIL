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
}
