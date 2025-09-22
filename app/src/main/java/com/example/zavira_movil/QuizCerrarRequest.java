package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizCerrarRequest {

    public static class RespuestaItem {
        @SerializedName("id_pregunta") public String idPregunta;
        @SerializedName("seleccion")   public String seleccion; // "A"/"B"/"C"/"D"
        public RespuestaItem(String idPregunta, String seleccion) {
            this.idPregunta = idPregunta; this.seleccion = seleccion;
        }
    }

    @SerializedName("id_sesion")  private String idSesion;
    @SerializedName("respuestas") private List<RespuestaItem> respuestas;

    public QuizCerrarRequest(String idSesion, List<RespuestaItem> respuestas) {
        this.idSesion = idSesion; this.respuestas = respuestas;
    }
}
