package com.example.zavira_movil.notifications;

/**
 * Wrapper para NotificationItem que extiende NotificationListItem
 */
public class NotificationItemWrapper extends NotificationListItem {
    private NotificationItem notification;
    private int originalPosition; // Posición en la lista original para eliminar

    public NotificationItemWrapper(NotificationItem notification, int originalPosition) {
        this.notification = notification;
        this.originalPosition = originalPosition;
    }

    public NotificationItem getNotification() {
        return notification;
    }

    public int getOriginalPosition() {
        return originalPosition;
    }

    public void setOriginalPosition(int position) {
        this.originalPosition = position;
    }

    @Override
    public int getType() {
        return TYPE_NOTIFICATION;
    }
}


