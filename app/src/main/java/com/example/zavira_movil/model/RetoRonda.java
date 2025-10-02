package com.example.zavira_movil.model;

public class RetoRonda {
    private int idReto;
    private int idPregunta;
    private int idRespuesta;

    public RetoRonda(int idReto, int idPregunta, int idRespuesta) {
        this.idReto = idReto;
        this.idPregunta = idPregunta;
        this.idRespuesta = idRespuesta;
    }

    public int getIdReto() { return idReto; }
    public void setIdReto(int idReto) { this.idReto = idReto; }

    public int getIdPregunta() { return idPregunta; }
    public void setIdPregunta(int idPregunta) { this.idPregunta = idPregunta; }

    public int getIdRespuesta() { return idRespuesta; }
    public void setIdRespuesta(int idRespuesta) { this.idRespuesta = idRespuesta; }
}
