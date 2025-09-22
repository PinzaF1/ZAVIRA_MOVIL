package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class Estudiante {

    // nombre
    @SerializedName(
            value = "nombre_usuario",
            alternate = {"nombreUsuario", "nombre", "firstName"}
    )
    private String nombreUsuario;

    // apellido
    @SerializedName(
            value = "apellido",
            alternate = {"lastName"}
    )
    private String apellido;

    // documento
    @SerializedName(
            value = "numero_documento",
            alternate = {"numeroDocumento", "documento", "doc"}
    )
    private String numeroDocumento;

    // grado
    @SerializedName(
            value = "grado",
            alternate = {"grade"}
    )
    private String grado;

    // curso
    @SerializedName(
            value = "curso",
            alternate = {"class", "cursoNombre"}
    )
    private String curso;

    // jornada
    @SerializedName(
            value = "jornada",
            alternate = {"session"}
    )
    private String jornada;

    // correo / email
    @SerializedName(
            value = "correo",
            alternate = {"email", "correoElectronico"}
    )
    private String correo;

    // foto perfil
    @SerializedName(
            value = "foto_url",
            alternate = {"fotoUrl", "foto", "profilePicture", "avatar"}
    )
    private String fotoUrl;

    // Getters
    public String getNombreUsuario()   { return nombreUsuario; }
    public String getApellido()        { return apellido; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getGrado()           { return grado; }
    public String getCurso()           { return curso; }
    public String getJornada()         { return jornada; }
    public String getCorreo()          { return correo; }
    public String getFotoUrl()         { return fotoUrl; }
}
