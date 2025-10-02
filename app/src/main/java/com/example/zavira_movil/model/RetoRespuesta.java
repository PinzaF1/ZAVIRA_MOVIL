package com.example.zavira_movil.model;

public class RetoRespuesta {
    private boolean acierto;
    private int puntajeActual;
    private int puntajeOponente;

    public boolean isAcierto() { return acierto; }
    public void setAcierto(boolean acierto) { this.acierto = acierto; }

    public int getPuntajeActual() { return puntajeActual; }
    public void setPuntajeActual(int puntajeActual) { this.puntajeActual = puntajeActual; }

    public int getPuntajeOponente() { return puntajeOponente; }
    public void setPuntajeOponente(int puntajeOponente) { this.puntajeOponente = puntajeOponente; }
}
