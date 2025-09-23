package com.example.zavira_movil.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Genera 5 preguntas de ejemplo por subtema.
 * La respuesta correcta siempre es la A (para simplificar la demo).
 * Cambia textos si quieres.
 */
public final class DemoQuestionBank {

    private DemoQuestionBank() {}

    public static List<Question> getFiveQuestions(String area, String subtema) {
        List<Question> out = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Question q = new Question();
            q.area = area;
            q.subtema = subtema;
            q.enunciado = subtema + " — Pregunta " + i;

            // opciones A..D (A correcta)
            q.addOption("A", "Opción A (correcta) " + i);
            q.addOption("B", "Opción B " + i);
            q.addOption("C", "Opción C " + i);
            q.addOption("D", "Opción D " + i);

            out.add(q);
        }
        return out;
    }

    /** Simulacro: 5 subtemas × 5 preguntas = 25 preguntas. */
    public static List<Question> getSimulacro25(String area, List<String> subtemasCinco) {
        List<Question> out = new ArrayList<>();
        if (subtemasCinco == null) return out;
        for (String sub : subtemasCinco) {
            out.addAll(getFiveQuestions(area, sub));
        }
        // Asegura 25 como máximo
        if (out.size() > 25) return new ArrayList<>(out.subList(0, 25));
        return out;
    }

    /** Evalúa: cuántas correctas (asumimos A correcta). */
    public static int evaluar(List<String> marcadas) {
        int correctas = 0;
        if (marcadas == null) return 0;
        for (String k : marcadas) {
            if ("A".equalsIgnoreCase(k)) correctas++;
        }
        return correctas;
    }
}
