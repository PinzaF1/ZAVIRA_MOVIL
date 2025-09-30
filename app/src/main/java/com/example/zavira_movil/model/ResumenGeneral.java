package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class ResumenGeneral {

    @SerializedName("progreso_general") // nombre exacto que viene del JSON
    private int progresoGeneral;

    public int getProgresoGeneral() {
        return progresoGeneral;
    }

    public void setProgresoGeneral(int progresoGeneral) {
        this.progresoGeneral = progresoGeneral;
    }
}
