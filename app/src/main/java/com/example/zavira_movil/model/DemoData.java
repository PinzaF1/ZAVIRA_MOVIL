package com.example.zavira_movil.model;

import com.example.zavira_movil.R;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DemoData {

    public static List<Subject> subjects() {
        return Arrays.asList(
                new Subject(
                        "1",
                        "Matemáticas",
                        "Desarrolla habilidades para resolver problemas numéricos, algebraicos, geométricos y estadísticos en contexto real.",
                        0, 5,
                        R.drawable.ic_book_24, R.drawable.bg_subject_header,
                        Arrays.asList(
                                new Subject.Level("Nivel 1 — Aritmética", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 2 — Álgebra", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 3 — Geometría", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 4 — Estadística y Probabilidad", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 5 — Funciones y Gráficas", "Pendiente", Collections.emptyList())
                        )
                ),
                new Subject(
                        "2",
                        "Lectura Crítica",
                        "Analiza y evalúa diferentes tipos de textos para identificar información explícita, implícita y la intención del autor.",
                        0, 5,
                        R.drawable.ic_book_24, R.drawable.bg_subject_header,
                        Arrays.asList(
                                new Subject.Level("Nivel 1 — Comprensión literal", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 2 — Vocabulario en contexto", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 3 — Comprensión inferencial", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 4 — Lectura crítica", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 5 — Tipos de texto", "Pendiente", Collections.emptyList())
                        )
                ),
                new Subject(
                        "3",
                        "Ciencias Sociales",
                        "Comprende procesos históricos, geográficos, económicos, políticos y culturales que explican la sociedad actual.",
                        0, 5,
                        R.drawable.ic_book_24, R.drawable.bg_subject_header,
                        Arrays.asList(
                                new Subject.Level("Nivel 1 — Historia", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 2 — Geografía", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 3 — Filosofía y ciudadanía", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 4 — Economía", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 5 — Cultura y sociedad", "Pendiente", Collections.emptyList())
                        )
                ),
                new Subject(
                        "4",
                        "Ciencias Naturales",
                        "Explica fenómenos de la biología, química, física y ciencias de la Tierra aplicados a la vida diaria.",
                        0, 5,
                        R.drawable.ic_book_24, R.drawable.bg_subject_header,
                        Arrays.asList(
                                new Subject.Level("Nivel 1 — Biología", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 2 — Química", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 3 — Física", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 4 — Ciencias de la Tierra", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 5 — Indagación científica", "Pendiente", Collections.emptyList())
                        )
                ),
                new Subject(
                        "5",
                        "Inglés",
                        "Desarrolla competencias de comprensión y producción en inglés, enfocadas en lectura y uso del lenguaje.",
                        0, 5,
                        R.drawable.ic_book_24, R.drawable.bg_subject_header,
                        Arrays.asList(
                                new Subject.Level("Nivel 1 — Grammar", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 2 — Vocabulary", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 3 — Reading Comprehension", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 4 — Listening", "Pendiente", Collections.emptyList()),
                                new Subject.Level("Nivel 5 — Writing", "Pendiente", Collections.emptyList())
                        )
                )
        );
    }
}
