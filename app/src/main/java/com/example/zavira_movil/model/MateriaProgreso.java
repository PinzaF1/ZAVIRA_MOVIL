package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class MateriaProgreso {

    @SerializedName("nombre")  // Coincide con tu JSON
    private String nombre;

    @SerializedName("porcentaje")
    private int porcentaje;

    public String getNombre() {
        return nombre;
    }

    public int getPorcentaje() {
        return porcentaje;
    }
}
