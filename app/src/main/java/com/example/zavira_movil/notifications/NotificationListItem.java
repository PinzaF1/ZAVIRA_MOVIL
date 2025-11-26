package com.example.zavira_movil.notifications;

/**
 * Clase base para items del RecyclerView que pueden ser
 * notificaciones o encabezados de fecha
 */
public abstract class NotificationListItem {
    public static final int TYPE_DATE_HEADER = 0;
    public static final int TYPE_NOTIFICATION = 1;

    public abstract int getType();
}

