package com.example.zavira_movil.model;

public class LoginRequest {
    private String numero_documento;
    private String password;

    public LoginRequest(String numero_documento, String password) {
        this.numero_documento = numero_documento;
        this.password = password;
    }
}
