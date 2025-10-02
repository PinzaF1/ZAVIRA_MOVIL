package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class Reto {

    @SerializedName("id")
    private int id;

    @SerializedName("idUsuarioRetado")
    private int idUsuarioRetado;

    @SerializedName("idMateria")
    private int idMateria;

    @SerializedName("dificultad")
    private String dificultad;

    // Constructor
    public Reto(int idUsuarioRetado, int idMateria, String dificultad) {
        this.idUsuarioRetado = idUsuarioRetado;
        this.idMateria = idMateria;
        this.dificultad = dificultad;
    }

    // Getter para 'id'
    public int getId() {
        return id;
    }

    // Otros getters
    public int getIdUsuarioRetado() {
        return idUsuarioRetado;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public String getDificultad() {
        return dificultad;
    }
}
