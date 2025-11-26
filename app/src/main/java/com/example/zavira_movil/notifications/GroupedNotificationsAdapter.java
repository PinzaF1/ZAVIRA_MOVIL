package com.example.zavira_movil.notifications;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zavira_movil.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adaptador mejorado con agrupación por fechas y soporte para eliminar
 */
public class GroupedNotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<NotificationListItem> items = new ArrayList<>();
    private List<NotificationItem> originalNotifications = new ArrayList<>();
    private OnNotificationClickListener clickListener;
    private OnNotificationDeleteListener deleteListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationItem notification, int originalPosition);
    }

    public interface OnNotificationDeleteListener {
        void onNotificationDelete(int originalPosition);
    }

    public GroupedNotificationsAdapter(OnNotificationClickListener clickListener, OnNotificationDeleteListener deleteListener) {
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    /**
     * Actualiza las notificaciones y las agrupa por fecha
     */
    public void setNotifications(List<NotificationItem> notifications) {
        this.originalNotifications = new ArrayList<>(notifications);
        this.items = groupNotificationsByDate(notifications);
        notifyDataSetChanged();
    }

    /**
     * Agrupa notificaciones por fecha (Hoy, Ayer, Hace X días)
     */
    private List<NotificationListItem> groupNotificationsByDate(List<NotificationItem> notifications) {
        List<NotificationListItem> groupedItems = new ArrayList<>();

        if (notifications.isEmpty()) {
            return groupedItems;
        }

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar yesterday = (Calendar) today.clone();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        String currentDateLabel = null;

        for (int i = 0; i < notifications.size(); i++) {
            NotificationItem notification = notifications.get(i);
            long timestamp = notification.getTimestamp();

            String dateLabel = getDateLabel(timestamp, today.getTimeInMillis(), yesterday.getTimeInMillis());

            // Agregar encabezado si cambia la fecha
            if (!dateLabel.equals(currentDateLabel)) {
                groupedItems.add(new DateHeaderItem(dateLabel));
                currentDateLabel = dateLabel;
            }

            // Agregar notificación
            groupedItems.add(new NotificationItemWrapper(notification, i));
        }

        return groupedItems;
    }

    /**
     * Obtiene la etiqueta de fecha (Hoy, Ayer, Hace X días, fecha específica)
     */
    private String getDateLabel(long timestamp, long todayMillis, long yesterdayMillis) {
        Calendar notifCal = Calendar.getInstance();
        notifCal.setTimeInMillis(timestamp);
        notifCal.set(Calendar.HOUR_OF_DAY, 0);
        notifCal.set(Calendar.MINUTE, 0);
        notifCal.set(Calendar.SECOND, 0);
        notifCal.set(Calendar.MILLISECOND, 0);

        long notifDayMillis = notifCal.getTimeInMillis();

        if (notifDayMillis == todayMillis) {
            return "Hoy";
        } else if (notifDayMillis == yesterdayMillis) {
            return "Ayer";
        } else {
            // Calcular días de diferencia
            long diffDays = (todayMillis - notifDayMillis) / (1000 * 60 * 60 * 24);

            if (diffDays <= 7) {
                return "Hace " + diffDays + " días";
            } else {
                // Mostrar fecha específica para notificaciones antiguas
                SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM", new Locale("es", "ES"));
                return sdf.format(new Date(timestamp));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NotificationListItem.TYPE_DATE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_notification, parent, false);
            return new NotificationViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        NotificationListItem item = items.get(position);

        if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).bind((DateHeaderItem) item);
        } else if (holder instanceof NotificationViewHolder) {
            NotificationItemWrapper wrapper = (NotificationItemWrapper) item;
            ((NotificationViewHolder) holder).bind(wrapper.getNotification(), wrapper.getOriginalPosition());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Elimina una notificación de la lista
     */
    public void removeNotification(int displayPosition) {
        if (displayPosition < 0 || displayPosition >= items.size()) return;

        NotificationListItem item = items.get(displayPosition);
        if (item instanceof NotificationItemWrapper) {
            int originalPosition = ((NotificationItemWrapper) item).getOriginalPosition();

            // Eliminar de la lista visual
            items.remove(displayPosition);

            // Verificar si el encabezado anterior quedó sin notificaciones
            if (displayPosition > 0 && items.size() > displayPosition) {
                NotificationListItem prevItem = items.get(displayPosition - 1);
                NotificationListItem nextItem = displayPosition < items.size() ? items.get(displayPosition) : null;

                // Si el anterior es un encabezado y el siguiente también (o no hay siguiente), eliminar el encabezado
                if (prevItem instanceof DateHeaderItem &&
                    (nextItem == null || nextItem instanceof DateHeaderItem)) {
                    items.remove(displayPosition - 1);
                    notifyItemRemoved(displayPosition - 1);
                }
            }

            notifyItemRemoved(displayPosition);

            // Notificar al listener
            if (deleteListener != null) {
                deleteListener.onNotificationDelete(originalPosition);
            }
        }
    }

    // ViewHolder para encabezados de fecha
    public static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDateHeader;

        public DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateHeader = itemView.findViewById(R.id.tvDateHeader);
        }

        public void bind(DateHeaderItem item) {
            tvDateHeader.setText(item.getDateLabel());
        }
    }

    // ViewHolder para notificaciones (reutiliza la lógica del adaptador original)
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private View iconBackground;
        private ImageView notificationIcon;
        private TextView notificationTitle;
        private TextView notificationMessage;
        private View retadorContainer;
        private TextView retadorNombre;
        private View notificationInfoContainer;
        private TextView notificationArea;
        private TextView notificationScore;
        private TextView notificationTime;
        private View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            notificationIcon = itemView.findViewById(R.id.notificationIcon);
            notificationTitle = itemView.findViewById(R.id.notificationTitle);
            notificationMessage = itemView.findViewById(R.id.notificationMessage);
            retadorContainer = itemView.findViewById(R.id.retadorContainer);
            retadorNombre = itemView.findViewById(R.id.retadorNombre);
            notificationInfoContainer = itemView.findViewById(R.id.notificationInfoContainer);
            notificationArea = itemView.findViewById(R.id.notificationArea);
            notificationScore = itemView.findViewById(R.id.notificationScore);
            notificationTime = itemView.findViewById(R.id.notificationTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }

        public void bind(NotificationItem notification, int originalPosition) {
            notificationTitle.setText(notification.getTitle());
            notificationMessage.setText(notification.getMessage());
            notificationTime.setText(notification.getTimeAgo());

            // Configurar ícono y color según el tipo
            NotificationStyle style = getNotificationStyle(notification);
            notificationIcon.setImageResource(style.iconRes);
            iconBackground.setBackgroundResource(style.backgroundRes);

            // Mostrar información del retador si es una notificación de reto
            if ("reto_recibido".equals(notification.getTipo()) && notification.getRetadorNombre() != null) {
                retadorContainer.setVisibility(View.VISIBLE);
                retadorNombre.setText("🎮 " + notification.getRetadorNombre() + " te ha retado");
            } else {
                retadorContainer.setVisibility(View.GONE);
            }

            // Mostrar información adicional si está disponible
            if (notification.getArea() != null && notification.getPuntaje() != null) {
                notificationInfoContainer.setVisibility(View.VISIBLE);
                notificationArea.setText(notification.getArea());
                notificationScore.setText(notification.getPuntaje() + "%");

                // Color del chip de puntaje según el valor
                int puntaje = Integer.parseInt(notification.getPuntaje());
                if (puntaje < 40) {
                    notificationScore.setBackgroundResource(R.drawable.bg_chip_red);
                    notificationScore.setTextColor(Color.parseColor("#EF4444"));
                } else if (puntaje < 70) {
                    notificationScore.setBackgroundResource(R.drawable.bg_chip_orange);
                    notificationScore.setTextColor(Color.parseColor("#F59E0B"));
                } else {
                    notificationScore.setBackgroundResource(R.drawable.bg_chip_green);
                    notificationScore.setTextColor(Color.parseColor("#22C55E"));
                }
            } else {
                notificationInfoContainer.setVisibility(View.GONE);
            }

            // Indicador de no leído
            if (notification.isRead()) {
                unreadIndicator.setVisibility(View.GONE);
                itemView.setAlpha(0.7f);
                itemView.setBackgroundResource(android.R.color.transparent);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
                itemView.setAlpha(1.0f);
                itemView.setBackgroundResource(R.drawable.bg_notification_unread);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onNotificationClick(notification, originalPosition);
                }
            });
        }

        private NotificationStyle getNotificationStyle(NotificationItem notification) {
            NotificationStyle style = new NotificationStyle();

            String tipo = notification.getTipo();
            if ("reto_recibido".equals(tipo)) {
                style.iconRes = R.drawable.ic_challenge;
                style.backgroundRes = R.drawable.bg_icon_purple;
            } else if ("reto_completado".equals(tipo)) {
                style.iconRes = R.drawable.ic_trophy;
                style.backgroundRes = R.drawable.bg_icon_gold;
            } else {
                style.iconRes = R.drawable.ic_notification;
                style.backgroundRes = R.drawable.bg_icon_blue;
            }

            return style;
        }
    }

    private static class NotificationStyle {
        int iconRes;
        int backgroundRes;
    }
}

