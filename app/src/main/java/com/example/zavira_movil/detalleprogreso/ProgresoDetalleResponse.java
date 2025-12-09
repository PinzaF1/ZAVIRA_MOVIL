package com.example.zavira_movil.detalleprogreso;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProgresoDetalleResponse {
    public Header header;
    public Resumen resumen;
    public List<Pregunta> preguntas;
    public Analisis analisis;

    public static class Header {
        public String materia;
        public String fecha;
        public String nivel;
        @SerializedName("nivelOrden")
        public Integer nivelOrden;  // Nullable para compatibilidad
        public int puntaje;
        public String escala;       // "porcentaje" o "ICFES"
        public int tiempo_total_seg;
        public int correctas;
        public int incorrectas;
        public int total;
    }

    public static class Resumen {
        public String cambio;
        public String mensaje;
        @SerializedName("nivelActual")
        public Integer nivelActual;  // Nullable para compatibilidad
    }

    public static class Pregunta {
        public int orden;
        @SerializedName("id_pregunta")
        public Integer id_pregunta;  // Nullable - puede ser null para preguntas de IA
        public String area;
        public String subtema;
        public String enunciado;
        public String correcta;          // "A"|"B"|"C"|"D"
        public String marcada;           // "A"|"B"|"C"|"D"
        public boolean es_correcta;
        public String explicacion;
        @SerializedName("tiempo_empleado_seg")
        public Integer tiempo_empleado_seg; // puede venir null
    }

    public static class Analisis {
        public List<String> fortalezas;
        @SerializedName("subtemas_a_mejorar")
        public List<String> subtemas_a_mejorar;  // Nuevo campo del backend
        public List<String> mejoras;
        public List<String> recomendaciones;
    }
}
