package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class HistorialItem {

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("detalle")
    private String detalle;

    @SerializedName(value = "fecha", alternate = {"created_at", "ultima_vez"})
    private String fecha;

    public String getTitulo() { return titulo; }
    public String getDetalle() { return detalle; }
    public String getFecha() { return fecha; }

    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    @SerializedName("id")
    private int id;

    @SerializedName("materia")
    private String materia;

    @SerializedName("porcentaje")
    private int porcentaje;

    @SerializedName("fecha")
    private String fecha; // Ej: "2025-10-01"

    public int getId() {
        return id;
    }

    public String getMateria() {
        return materia;
    }

    public int getPorcentaje() {
        return porcentaje;
    }

    public String getFecha() {
        return fecha;
    }

}
