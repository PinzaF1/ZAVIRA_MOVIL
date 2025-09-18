package com.example.zavira_movil.model;

import java.util.List;

public class KolbRequest {
    private int id_usuario;
    private List<Respuesta> respuestas;

    public KolbRequest(int id_usuario, List<Respuesta> respuestas) {
        this.id_usuario = id_usuario;
        this.respuestas = respuestas;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public List<Respuesta> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<Respuesta> respuestas) {
        this.respuestas = respuestas;
    }

    public static class Respuesta {
        private int id_pregunta;
        private int valor;

        public Respuesta(int id_pregunta, int valor) {
            this.id_pregunta = id_pregunta;
            this.valor = valor;
        }

        public int getId_pregunta() {
            return id_pregunta;
        }

        public void setId_pregunta(int id_pregunta) {
            this.id_pregunta = id_pregunta;
        }

        public int getValor() {
            return valor;
        }

        public void setValor(int valor) {
            this.valor = valor;
        }


        public void setRespuesta(int valor) {
            this.valor = valor;
        }
    }
}