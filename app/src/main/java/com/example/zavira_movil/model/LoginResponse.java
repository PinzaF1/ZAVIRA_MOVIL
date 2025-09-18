package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName(value = "token", alternate = {"accessToken", "jwt"})
    private String token;

    private String estudiante; //

    private String error;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEstudiante() { return estudiante; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
