package com.example.zavira_movil.model;

import com.example.zavira_movil.R;
import java.util.Arrays;
import java.util.List;

public class DemoData {

    // ---- Subtemas de ejemplo (Matemáticas) ----
    private static List<Subject.Subtopic> subtemasMate1() {
        return Arrays.asList(
                new Subject.Subtopic("Operaciones con enteros", true),
                new Subject.Subtopic("Proporciones y razones", true),
                new Subject.Subtopic("Porcentajes", false)
        );
    }
    private static List<Subject.Subtopic> subtemasMate2() {
        return Arrays.asList(
                new Subject.Subtopic("Expresiones algebraicas", true),
                new Subject.Subtopic("Ecuaciones lineales", false),
                new Subject.Subtopic("Sistemas de ecuaciones", false)
        );
    }
    private static List<Subject.Subtopic> subtemasMate3() {
        return Arrays.asList(
                new Subject.Subtopic("Figuras planas", false),
                new Subject.Subtopic("Perímetro y área", false),
                new Subject.Subtopic("Ángulos", false)
        );
    }

    // Repite helpers similares si quieres subtemas para las otras materias

    public static List<Subject> subjects() {
        return Arrays.asList(
                new Subject("MAT", "Matemáticas", 12, 25, R.drawable.ic_book_24,
                        R.drawable.bg_header_math,
                        Arrays.asList(
                                new Subject.Level("Nivel 1: Números y proporciones", "Completado", subtemasMate1()),
                                new Subject.Level("Nivel 2: Álgebra básica", "En curso", subtemasMate2()),
                                new Subject.Level("Nivel 3: Geometría y medidas", "Bloqueado", subtemasMate3())
                        )),
                new Subject("LECT", "Lectura Crítica", 8, 20, R.drawable.ic_book_24,
                        R.drawable.bg_header_reading,
                        Arrays.asList(
                                new Subject.Level("Nivel 1: Comprensión literal", "Completado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Ideas principales", true),
                                                new Subject.Subtopic("Detalles explícitos", true),
                                                new Subject.Subtopic("Conectores básicos", false)
                                        )),
                                new Subject.Level("Nivel 2: Inferencia", "En curso",
                                        Arrays.asList(
                                                new Subject.Subtopic("Inferencias causales", false),
                                                new Subject.Subtopic("Implicaturas", false),
                                                new Subject.Subtopic("Contexto y tono", false)
                                        )),
                                new Subject.Level("Nivel 3: Argumentación", "Bloqueado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Estructura del argumento", false),
                                                new Subject.Subtopic("Falacias comunes", false),
                                                new Subject.Subtopic("Posturas del autor", false)
                                        ))
                        )),
                new Subject("SOC", "Sociales y Ciudadanas", 18, 28, R.drawable.ic_book_24,
                        R.drawable.bg_header_social,
                        Arrays.asList(
                                new Subject.Level("Nivel 1: Ciudadanía y Estado", "Completado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Derechos y deberes", true),
                                                new Subject.Subtopic("Participación democrática", false),
                                                new Subject.Subtopic("Organización del Estado", false)
                                        )),
                                new Subject.Level("Nivel 2: Historia de Colombia", "En curso",
                                        Arrays.asList(
                                                new Subject.Subtopic("Independencia de Colombia", false),
                                                new Subject.Subtopic("Siglo XX en Colombia", false),
                                                new Subject.Subtopic("Procesos sociales", false)
                                        )),
                                new Subject.Level("Nivel 3: Constitución", "Bloqueado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Constitución de 1991", false),
                                                new Subject.Subtopic("Ramas del poder público", false),
                                                new Subject.Subtopic("Mecanismos de participación", false)
                                        ))
                        )),
                new Subject("NAT", "Ciencias Naturales", 10, 24, R.drawable.ic_book_24,
                        R.drawable.bg_header_science,
                        Arrays.asList(
                                new Subject.Level("Nivel 1: Biología", "En curso",
                                        Arrays.asList(
                                                new Subject.Subtopic("Célula y tejidos", true),
                                                new Subject.Subtopic("Genética básica", false),
                                                new Subject.Subtopic("Ecosistemas", false)
                                        )),
                                new Subject.Level("Nivel 2: Física", "Bloqueado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Movimiento y fuerzas", false),
                                                new Subject.Subtopic("Energía", false),
                                                new Subject.Subtopic("Ondas", false)
                                        )),
                                new Subject.Level("Nivel 3: Química", "Bloqueado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Propiedades de la materia", false),
                                                new Subject.Subtopic("Reacciones químicas", false),
                                                new Subject.Subtopic("Estequiometría", false)
                                        ))
                        )),
                new Subject("ING", "Inglés", 5, 18, R.drawable.ic_book_24,
                        R.drawable.bg_header_english,
                        Arrays.asList(
                                new Subject.Level("Nivel 1: Reading A1", "Completado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Reading: food & drinks", true),
                                                new Subject.Subtopic("Reading: hobbies", false),
                                                new Subject.Subtopic("Reading: travel", false)
                                        )),
                                new Subject.Level("Nivel 2: Vocabulary A2", "En curso",
                                        Arrays.asList(
                                                new Subject.Subtopic("Vocabulary: daily routines", false),
                                                new Subject.Subtopic("Vocabulary: places in town", false),
                                                new Subject.Subtopic("Vocabulary: adjectives", false)
                                        )),
                                new Subject.Level("Nivel 3: Listening B1", "Bloqueado",
                                        Arrays.asList(
                                                new Subject.Subtopic("Listening: short dialogues", false),
                                                new Subject.Subtopic("Listening: announcements", false),
                                                new Subject.Subtopic("Listening: interviews", false)
                                        ))
                        ))
        );
    }
}
