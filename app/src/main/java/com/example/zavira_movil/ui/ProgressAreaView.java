package com.example.zavira_movil.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.zavira_movil.R;

public class ProgressAreaView extends View {
    
    private String[] areas = {"MAT", "LENG", "CIEN", "SOC", "ING"};
    private int[] colores = {
        // Inicialización por defecto; serán reemplazados en onSizeChanged o antes de dibujar
        android.graphics.Color.parseColor("#B6B9C2"),
        android.graphics.Color.parseColor("#B6B9C2"),
        android.graphics.Color.parseColor("#B6B9C2"),
        android.graphics.Color.parseColor("#B6B9C2"),
        android.graphics.Color.parseColor("#B6B9C2")
    };
    private int areaActual = 0;
    
    private Paint paintActivo;
    private Paint paintInactivo;
    private Paint paintTextoActivo;
    private Paint paintTextoInactivo;
    
    public ProgressAreaView(Context context) {
        super(context);
        init();
    }
    
    public ProgressAreaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        paintActivo = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintActivo.setStyle(Paint.Style.FILL);
        
        paintInactivo = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintInactivo.setStyle(Paint.Style.FILL);
        paintInactivo.setColor(Color.parseColor("#E0E0E0"));
        
        paintTextoActivo = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextoActivo.setColor(Color.WHITE);
        paintTextoActivo.setTextSize(32);
        paintTextoActivo.setTextAlign(Paint.Align.CENTER);
        paintTextoActivo.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        
        paintTextoInactivo = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintTextoInactivo.setColor(Color.parseColor("#9E9E9E"));
        paintTextoInactivo.setTextSize(32);
        paintTextoInactivo.setTextAlign(Paint.Align.CENTER);
        paintTextoInactivo.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // Actualizar la paleta desde AreaColorManager cuando la vista se adjunta
        try {
            com.example.zavira_movil.util.AreaColorManager m = new com.example.zavira_movil.util.AreaColorManager();
        } catch (Exception ignored) {}
        actualizarColoresDesdeRecursos();
    }

    private void actualizarColoresDesdeRecursos() {
        // Mapear cada índice a su área correspondiente y obtener color via AreaColorManager
        // Orden: MAT, LENG, CIEN, SOC, ING
        try {
            android.content.Context c = getContext();
            colores[0] = com.example.zavira_movil.util.AreaColorManager.getColor(c, "Matematicas");
            colores[1] = com.example.zavira_movil.util.AreaColorManager.getColor(c, "Lenguaje");
            colores[2] = com.example.zavira_movil.util.AreaColorManager.getColor(c, "Ciencias");
            colores[3] = com.example.zavira_movil.util.AreaColorManager.getColor(c, "Sociales");
            colores[4] = com.example.zavira_movil.util.AreaColorManager.getColor(c, "Ingles");
        } catch (Exception e) {
            // Silencioso: mantener colores por defecto si falla
        }
    }

    public void setAreaActual(int index) {
        this.areaActual = index;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        float width = getWidth();
        float height = getHeight();
        float segmentWidth = width / areas.length;
        float arrowHeight = height;
        
        for (int i = 0; i < areas.length; i++) {
            boolean esActivo = i <= areaActual;
            Paint paintFondo = esActivo ? paintActivo : paintInactivo;
            Paint paintTexto = esActivo ? paintTextoActivo : paintTextoInactivo;
            
            if (esActivo) {
                paintFondo.setColor(colores[i]);
            }
            
            float x = i * segmentWidth;
            float xNext = (i + 1) * segmentWidth;
            
            // Dibujar forma de flecha
            Path path = new Path();
            
            if (i == 0) {
                // Primera flecha: esquina redondeada izquierda
                path.moveTo(x + 20, 0);
                path.lineTo(xNext - 30, 0);
                path.lineTo(xNext, arrowHeight / 2);
                path.lineTo(xNext - 30, arrowHeight);
                path.lineTo(x + 20, arrowHeight);
                path.quadTo(x, arrowHeight, x, arrowHeight - 20);
                path.lineTo(x, 20);
                path.quadTo(x, 0, x + 20, 0);
            } else if (i == areas.length - 1) {
                // Última flecha: esquina redondeada derecha
                path.moveTo(x, 0);
                path.lineTo(xNext - 20, 0);
                path.quadTo(xNext, 0, xNext, 20);
                path.lineTo(xNext, arrowHeight - 20);
                path.quadTo(xNext, arrowHeight, xNext - 20, arrowHeight);
                path.lineTo(x, arrowHeight);
                path.lineTo(x + 30, arrowHeight / 2);
                path.close();
            } else {
                // Flechas intermedias
                path.moveTo(x, 0);
                path.lineTo(xNext - 30, 0);
                path.lineTo(xNext, arrowHeight / 2);
                path.lineTo(xNext - 30, arrowHeight);
                path.lineTo(x, arrowHeight);
                path.lineTo(x + 30, arrowHeight / 2);
                path.close();
            }
            
            canvas.drawPath(path, paintFondo);
            
            // Dibujar texto centrado
            float textX = x + segmentWidth / 2;
            float textY = arrowHeight / 2 + 15; // Ajuste vertical para centrar
            canvas.drawText(areas[i], textX, textY, paintTexto);
        }
    }
}
