package com.example.zavira_movil.model;

import java.util.List;

public class RetoEstado {
    private int idReto;
    private String estado;
    private List<String> jugadores;
    private int puntajeJugador1;
    private int puntajeJugador2;

    public int getIdReto() { return idReto; }
    public void setIdReto(int idReto) { this.idReto = idReto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<String> getJugadores() { return jugadores; }
    public void setJugadores(List<String> jugadores) { this.jugadores = jugadores; }

    public int getPuntajeJugador1() { return puntajeJugador1; }
    public void setPuntajeJugador1(int puntajeJugador1) { this.puntajeJugador1 = puntajeJugador1; }

    public int getPuntajeJugador2() { return puntajeJugador2; }
    public void setPuntajeJugador2(int puntajeJugador2) { this.puntajeJugador2 = puntajeJugador2; }
}
