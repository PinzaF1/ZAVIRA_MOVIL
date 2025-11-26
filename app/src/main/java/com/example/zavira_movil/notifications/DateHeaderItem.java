package com.example.zavira_movil.notifications;

/**
 * Item de encabezado de fecha para agrupar notificaciones
 */
public class DateHeaderItem extends NotificationListItem {
    private String dateLabel;
    
    public DateHeaderItem(String dateLabel) {
        this.dateLabel = dateLabel;
    }
    
    public String getDateLabel() {
        return dateLabel;
    }
    
    @Override
    public int getType() {
        return TYPE_DATE_HEADER;
    }
}
