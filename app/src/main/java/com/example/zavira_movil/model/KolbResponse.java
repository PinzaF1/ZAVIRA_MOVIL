package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class KolbResponse {

    @SerializedName("mensaje")
    private String mensaje;

    @SerializedName("estilo_dominante")
    private String estiloDominante;

    @SerializedName("estilo")
    private String estilo;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido")
    private String apellido;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("caracteristicas")
    private String caracteristicas;

    @SerializedName("recomendaciones")
    private String recomendaciones;

    // ----- Getters y Setters -----
    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEstiloDominante() {
        return estiloDominante;
    }

    public void setEstiloDominante(String estiloDominante) {
        this.estiloDominante = estiloDominante;
    }

    public String getEstilo() {
        return estilo;
    }

    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(String caracteristicas) {
        this.caracteristicas = caracteristicas;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }
}
