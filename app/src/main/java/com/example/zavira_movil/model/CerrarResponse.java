// CerrarResponse.java
package com.example.zavira_movil.model;
import com.google.gson.annotations.SerializedName;

public class CerrarResponse {
    @SerializedName("puntaje")   public Integer puntaje;
    @SerializedName("correctas") public Integer correctas;
    @SerializedName("aprueba")   public Boolean aprueba;
}
