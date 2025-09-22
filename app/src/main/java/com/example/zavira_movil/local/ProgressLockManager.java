package com.example.zavira_movil.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public final class ProgressLockManager {

    private static final String PREFS = "progress_lock_prefs";
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 5;

    private ProgressLockManager() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(String area) {
        if (TextUtils.isEmpty(area)) area = "default_area";
        return "unlocked_level_" + area.trim();
    }

    /** Nivel más alto desbloqueado para esa área (1..5). Por defecto 1. */
    public static int getUnlockedLevel(Context c, String area) {
        int v = prefs(c).getInt(key(area), MIN_LEVEL);
        if (v < MIN_LEVEL) v = MIN_LEVEL;
        if (v > MAX_LEVEL) v = MAX_LEVEL;
        return v;
    }

    /** true si ese nivel está desbloqueado. */
    public static boolean isLevelUnlocked(Context c, String area, int level) {
        return level <= getUnlockedLevel(c, area);
    }

    /** Desbloquea el siguiente nivel (si current aprueba). Máximo 5. */
    public static void unlockNext(Context c, String area, int currentLevel) {
        int now = getUnlockedLevel(c, area);
        int target = Math.max(now, Math.min(MAX_LEVEL, currentLevel + 1));
        prefs(c).edit().putInt(key(area), target).apply();
    }

    /** (Opcional) Forzar set de nivel desbloqueado (para pruebas). */
    public static void setUnlockedLevel(Context c, String area, int level) {
        int target = Math.max(MIN_LEVEL, Math.min(MAX_LEVEL, level));
        prefs(c).edit().putInt(key(area), target).apply();
    }

    /** (Opcional) Reset a nivel 1. */
    public static void reset(Context c, String area) {
        prefs(c).edit().putInt(key(area), MIN_LEVEL).apply();
    }
}
