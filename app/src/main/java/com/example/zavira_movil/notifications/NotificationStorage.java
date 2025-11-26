package com.example.zavira_movil.notifications;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationStorage {
    private static final String PREFS_NAME = "notifications_prefs";
    private static final String KEY_NOTIFICATIONS = "notifications_list";
    private static final int MAX_NOTIFICATIONS = 50; // Límite de notificaciones guardadas
    
    private final SharedPreferences prefs;
    private final Gson gson;
    
    public NotificationStorage(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }
    
    /**
     * Guarda una nueva notificación
     */
    public void saveNotification(NotificationItem notification) {
        android.util.Log.d("NotificationStorage", "📥 saveNotification() llamado");
        android.util.Log.d("NotificationStorage", "  • Tipo: " + notification.getTipo());
        android.util.Log.d("NotificationStorage", "  • Título: " + notification.getTitle());
        android.util.Log.d("NotificationStorage", "  • Retador: " + notification.getRetadorNombre());

        List<NotificationItem> notifications = getAllNotifications();
        android.util.Log.d("NotificationStorage", "  • Notificaciones actuales: " + notifications.size());

        // Agregar al inicio de la lista
        notifications.add(0, notification);
        
        // Limitar el número de notificaciones
        if (notifications.size() > MAX_NOTIFICATIONS) {
            notifications = notifications.subList(0, MAX_NOTIFICATIONS);
        }
        
        // Guardar en SharedPreferences
        String json = gson.toJson(notifications);
        android.util.Log.d("NotificationStorage", "  • JSON generado (primeros 200 chars): " +
            (json.length() > 200 ? json.substring(0, 200) + "..." : json));

        boolean success = prefs.edit().putString(KEY_NOTIFICATIONS, json).commit();
        android.util.Log.d("NotificationStorage", "  • Guardado exitoso: " + success);
        android.util.Log.d("NotificationStorage", "  • Total notificaciones ahora: " + notifications.size());
    }
    
    /**
     * Obtiene todas las notificaciones
     */
    public List<NotificationItem> getAllNotifications() {
        String json = prefs.getString(KEY_NOTIFICATIONS, null);
        
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        
        Type type = new TypeToken<List<NotificationItem>>(){}.getType();
        List<NotificationItem> notifications = gson.fromJson(json, type);
        
        return notifications != null ? notifications : new ArrayList<>();
    }
    
    /**
     * Obtiene notificaciones de los últimos N días
     */
    public List<NotificationItem> getNotificationsByDays(int days) {
        List<NotificationItem> allNotifications = getAllNotifications();
        List<NotificationItem> filtered = new ArrayList<>();
        
        long now = System.currentTimeMillis();
        long daysInMillis = days * 24L * 60L * 60L * 1000L;
        
        for (NotificationItem notification : allNotifications) {
            if (now - notification.getTimestamp() <= daysInMillis) {
                filtered.add(notification);
            }
        }
        
        return filtered;
    }
    
    /**
     * Marca una notificación como leída
     */
    public void markAsRead(int position) {
        List<NotificationItem> notifications = getAllNotifications();
        
        if (position >= 0 && position < notifications.size()) {
            notifications.get(position).setRead(true);
            
            String json = gson.toJson(notifications);
            prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
        }
    }
    
    /**
     * Obtiene el número de notificaciones no leídas
     */
    public int getUnreadCount() {
        List<NotificationItem> notifications = getAllNotifications();
        int count = 0;
        
        for (NotificationItem notification : notifications) {
            if (!notification.isRead()) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Marca todas las notificaciones como leídas
     */
    public void markAllAsRead() {
        List<NotificationItem> notifications = getAllNotifications();

        for (NotificationItem notification : notifications) {
            notification.setRead(true);
        }

        String json = gson.toJson(notifications);
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
    }

    /**
     * Elimina una notificación específica por posición
     */
    public void deleteNotification(int position) {
        List<NotificationItem> notifications = getAllNotifications();

        if (position >= 0 && position < notifications.size()) {
            notifications.remove(position);

            String json = gson.toJson(notifications);
            prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
        }
    }

    /**
     * Elimina notificaciones antiguas (más viejas que X días)
     */
    public int deleteOlderThan(int days) {
        List<NotificationItem> notifications = getAllNotifications();
        long now = System.currentTimeMillis();
        long daysInMillis = days * 24L * 60L * 60L * 1000L;

        List<NotificationItem> filtered = new ArrayList<>();
        int deletedCount = 0;

        for (NotificationItem notification : notifications) {
            if (now - notification.getTimestamp() <= daysInMillis) {
                filtered.add(notification);
            } else {
                deletedCount++;
            }
        }

        if (deletedCount > 0) {
            String json = gson.toJson(filtered);
            prefs.edit().putString(KEY_NOTIFICATIONS, json).apply();
        }

        return deletedCount;
    }

    /**
     * Elimina todas las notificaciones
     */
    public void clearAll() {
        prefs.edit().remove(KEY_NOTIFICATIONS).apply();
    }
}
