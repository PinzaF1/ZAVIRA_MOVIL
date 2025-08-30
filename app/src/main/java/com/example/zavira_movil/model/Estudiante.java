package com.example.zavira_movil.model;
import com.google.gson.annotations.SerializedName;
public class Estudiante {

    @SerializedName(value = "nombre_usuario", alternate = {"nombre","firstName"})
    private String nombreUsuario;

    @SerializedName(value = "apellido", alternate = {"lastName"})
    private String apellido;

    @SerializedName(value = "numero_documento", alternate = {"documento","doc"})
    private String numeroDocumento;

    private String grado;
    private String curso;
    private String jornada;
    private String correo;

    // Getters
    public String getNombreUsuario() { return nombreUsuario; }
    public String getApellido() { return apellido; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getGrado() { return grado; }
    public String getCurso() { return curso; }
    public String getJornada() { return jornada; }
    public String getCorreo() { return correo; }
}
