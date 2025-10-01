package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class Nivel {
    @SerializedName("nombre") public String nombre;
    @SerializedName("min") public int min;
    @SerializedName("max") public int max;
    @SerializedName("rango") public String rango;
    @SerializedName("actual") public boolean actual;
    @SerializedName("valor") public int valor;
}
