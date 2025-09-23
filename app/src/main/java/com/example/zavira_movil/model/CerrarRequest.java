// CerrarRequest.java
package com.example.zavira_movil.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class CerrarRequest {
    @SerializedName(value = "id_sesion", alternate = {"idSesion"})
    public int id_sesion;
    @SerializedName("respuestas")
    public List<Respuesta> respuestas;
    public CerrarRequest(int idSesion, List<Respuesta> respuestas) {
        this.id_sesion = idSesion; this.respuestas = respuestas;
    }
    public static class Respuesta {
        @SerializedName("orden")  public int orden;
        @SerializedName("opcion") public String opcion;
        public Respuesta(int orden, String opcion) { this.orden = orden; this.opcion = opcion; }
    }
}
