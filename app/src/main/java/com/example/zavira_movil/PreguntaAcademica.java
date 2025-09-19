package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class PreguntaAcademica {

    @SerializedName("id_pregunta") private String idPregunta;
    @SerializedName("area")        private String area;
    @SerializedName("subtema")     private String subtema;
    @SerializedName("dificultad")  private String dificultad;
    @SerializedName("enunciado")   private String enunciado;
    @SerializedName("opciones")    private List<Opcion> opciones;

    public String getIdPregunta() { return idPregunta; }
    public String getArea() { return area == null ? "" : area; }
    public String getSubtema() { return subtema == null ? "" : subtema; }
    public String getDificultad() { return dificultad == null ? "" : dificultad; }
    public String getEnunciado() { return enunciado == null ? "" : enunciado; }
    public List<Opcion> getOpciones() { return opciones == null ? new ArrayList<>() : opciones; }
}
