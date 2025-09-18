package com.example.zavira_movil.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("numero_documento")
    private String numeroDocumento;

    @SerializedName("password")
    private String password;

    public LoginRequest(String numeroDocumento, String password) {
        this.numeroDocumento = numeroDocumento;
        this.password = password;
    }
}

