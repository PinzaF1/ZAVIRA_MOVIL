package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class PreguntasKolb {

    // === Campos exactos del JSON ===
    @SerializedName("idPreguntaEstiloAprendizajes")
    private Integer idPreguntaEstiloAprendizajes;

    @SerializedName("tipoPregunta")
    private String tipoPregunta;

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("pregunta")
    private String pregunta;


    public int getId_pregunta_estilo_aprendizajes() {
        return idPreguntaEstiloAprendizajes != null ? idPreguntaEstiloAprendizajes : 0;
    }

    public String getTipo_pregunta() {
        return tipoPregunta != null ? tipoPregunta : "";
    }

    public String getTitulo() {
        return titulo != null ? titulo : "";
    }

    public String getPregunta() {
        return pregunta != null ? pregunta : "";
    }


    public void setIdPreguntaEstiloAprendizajes(Integer id) {
        this.idPreguntaEstiloAprendizajes = id;
    }

    public void setTipoPregunta(String tipoPregunta) {
        this.tipoPregunta = tipoPregunta;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setPregunta(String pregunta) {
        this.pregunta = pregunta;
    }
}