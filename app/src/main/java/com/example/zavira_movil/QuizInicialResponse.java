package com.example.zavira_movil;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizInicialResponse {
    @SerializedName("id_sesion") private String idSesion;
    @SerializedName("preguntas") private List<PreguntaAcademica> preguntas;

    public String getIdSesion() { return idSesion; }
    public List<PreguntaAcademica> getPreguntas() { return preguntas; }
}
