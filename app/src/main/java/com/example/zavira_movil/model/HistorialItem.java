package com.example.zavira_movil.model;

public class HistorialItem {
    private String fecha; // Ej: "2025-09-30"
    private String actividad;
    private boolean completada;

    // Getters y Setters
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getActividad() { return actividad; }
    public void setActividad(String actividad) { this.actividad = actividad; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}
