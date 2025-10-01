package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class ResumenGeneral {

    @SerializedName(value = "progresoGeneral", alternate = {"progreso_general", "progreso"})
    private int progresoGeneral;

    public int getProgresoGeneral() { return progresoGeneral; }
    public void setProgresoGeneral(int progresoGeneral) { this.progresoGeneral = progresoGeneral; }


    @SerializedName("porcentajeCompletado")
    private float porcentajeCompletado;   // Progreso global

    @SerializedName("nivelActual")
    private String nivelActual;            // Nivel actual (Básico, Intermedio...)

    @SerializedName("progresoBasico")
    private float progresoBasico;          // Progreso barra nivel básico

    @SerializedName("progresoIntermedio")
    private float progresoIntermedio;      // Progreso barra nivel intermedio

    @SerializedName("progresoAvanzado")
    private float progresoAvanzado;        // Progreso barra nivel avanzado

    @SerializedName("progresoExperto")
    private float progresoExperto;         // Progreso barra nivel experto

    // Constructor vacío (necesario para Retrofit/Gson)
    public ResumenGeneral() { }

    // Getters y Setters
    public float getPorcentajeCompletado() {
        return porcentajeCompletado;
    }

    public void setPorcentajeCompletado(float porcentajeCompletado) {
        this.porcentajeCompletado = porcentajeCompletado;
    }

    public String getNivelActual() {
        return nivelActual;
    }

    public void setNivelActual(String nivelActual) {
        this.nivelActual = nivelActual;
    }

    public float getProgresoBasico() {
        return progresoBasico;
    }

    public void setProgresoBasico(float progresoBasico) {
        this.progresoBasico = progresoBasico;
    }

    public float getProgresoIntermedio() {
        return progresoIntermedio;
    }

    public void setProgresoIntermedio(float progresoIntermedio) {
        this.progresoIntermedio = progresoIntermedio;
    }

    public float getProgresoAvanzado() {
        return progresoAvanzado;
    }

    public void setProgresoAvanzado(float progresoAvanzado) {
        this.progresoAvanzado = progresoAvanzado;
    }

    public float getProgresoExperto() {
        return progresoExperto;
    }

    public void setProgresoExperto(float progresoExperto) {
        this.progresoExperto = progresoExperto;
    }

}
