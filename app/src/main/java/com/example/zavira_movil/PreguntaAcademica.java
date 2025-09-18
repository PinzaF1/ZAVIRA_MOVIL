package com.example.zavira_movil;

import java.util.ArrayList;
import java.util.List;

public class PreguntaAcademica {
    private int id_pregunta;
    private String area;
    private String pregunta;
    private String opcion_a;
    private String opcion_b;
    private String opcion_c;
    private String opcion_d;
    private String respuesta_cor;
    private String explicacion;
    private int time_limit_sec;


    public List<String> getOpciones() {
        List<String> opciones = new ArrayList<>();
        if (opcion_a != null) opciones.add(opcion_a);
        if (opcion_b != null) opciones.add(opcion_b);
        if (opcion_c != null) opciones.add(opcion_c);
        if (opcion_d != null) opciones.add(opcion_d);
        return opciones;
    }

    // Getters y Setters
    public int getId_pregunta() { return id_pregunta; }
    public void setId_pregunta(int id_pregunta) { this.id_pregunta = id_pregunta; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getPregunta() { return pregunta; }
    public void setPregunta(String pregunta) { this.pregunta = pregunta; }

    public String getOpcion_a() { return opcion_a; }
    public void setOpcion_a(String opcion_a) { this.opcion_a = opcion_a; }

    public String getOpcion_b() { return opcion_b; }
    public void setOpcion_b(String opcion_b) { this.opcion_b = opcion_b; }

    public String getOpcion_c() { return opcion_c; }
    public void setOpcion_c(String opcion_c) { this.opcion_c = opcion_c; }

    public String getOpcion_d() { return opcion_d; }
    public void setOpcion_d(String opcion_d) { this.opcion_d = opcion_d; }

    public String getRespuesta_cor() { return respuesta_cor; }
    public void setRespuesta_cor(String respuesta_cor) { this.respuesta_cor = respuesta_cor; }

    public String getExplicacion() { return explicacion; }
    public void setExplicacion(String explicacion) { this.explicacion = explicacion; }

    public int getTime_limit_sec() { return time_limit_sec; }
    public void setTime_limit_sec(int time_limit_sec) { this.time_limit_sec = time_limit_sec; }
}
