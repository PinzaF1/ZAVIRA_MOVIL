package com.example.zavira_movil.niveleshome;

import java.util.List;

public class ReportIaRequest {
    public Integer id_sesion;
    public Integer user_id; // opcional
    public String area;
    public String subtema;
    public Integer nivel;
    public String detected_by; // e.g. "client-heuristic"
    public boolean is_ia; // true si el cliente detectó IA (pero en nuestro uso será false)
    public String reason; // breve motivo o regla (e.g. "id_pregunta_present")
    public List<ReportQuestion> preguntas;
    public String timestamp; // ISO timestamp opcional

    public static class ReportQuestion {
        public int orden;
        public Integer id_pregunta; // puede ser null
        public String enunciado_preview;
        public boolean is_ia_likely;

        public ReportQuestion(int orden, Integer id_pregunta, String enunciado_preview, boolean is_ia_likely) {
            this.orden = orden;
            this.id_pregunta = id_pregunta;
            this.enunciado_preview = enunciado_preview;
            this.is_ia_likely = is_ia_likely;
        }
    }

    public ReportIaRequest(Integer id_sesion, Integer user_id, String area, String subtema, Integer nivel, String detected_by, boolean is_ia, String reason, List<ReportQuestion> preguntas, String timestamp) {
        this.id_sesion = id_sesion;
        this.user_id = user_id;
        this.area = area;
        this.subtema = subtema;
        this.nivel = nivel;
        this.detected_by = detected_by;
        this.is_ia = is_ia;
        this.reason = reason;
        this.preguntas = preguntas;
        this.timestamp = timestamp;
    }
}

