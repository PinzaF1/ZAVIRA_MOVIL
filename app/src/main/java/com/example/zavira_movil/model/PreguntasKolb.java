package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class PreguntasKolb {

    @SerializedName("id_pregunta_estilo_aprendizajes")
    private int id_pregunta_estilo_aprendizajes;

    @SerializedName("tipoPregunta")
    private String tipo_pregunta;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("pregunta")
    private String pregunta;

    public int getId_pregunta_estilo_aprendizajes() {
        return id_pregunta_estilo_aprendizajes;
    }

    public void setId_pregunta_estilo_aprendizajes(int id_pregunta_estilo_aprendizajes) {
        this.id_pregunta_estilo_aprendizajes = id_pregunta_estilo_aprendizajes;
    }

    public String getTipo_pregunta() {
        return tipo_pregunta;
    }

    public void setTipo_pregunta(String tipo_pregunta) {
        this.tipo_pregunta = tipo_pregunta;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getPregunta() {
        return pregunta;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }
}
